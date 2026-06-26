package dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;

import connection.Conexao;
import model.Endereco;
import model.Usuario;

public class UsuarioDAO {

    public void cadastrarUsuarioCompleto(Usuario usuario, Endereco endereco) throws SQLException {

        String sql = "{CALL sp_transacao_cadastro_completo(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        Connection conn = Conexao.getConexaoAtual();

        if (conn == null) {
            throw new SQLException("Não há conexão ativa com o banco de dados.");
        }

        try (CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getCpf());
            stmt.setString(3, usuario.getEmail());
            stmt.setString(4, usuario.getSenha());
            stmt.setString(5, usuario.getTipo().name());

            stmt.setString(6, endereco.getLogradouro());
            stmt.setString(7, endereco.getBairro());
            stmt.setString(8, endereco.getCidade());
            stmt.setString(9, endereco.getUf());

            stmt.execute();

            String login = usuario.getEmail();

            try (Statement createUserStmt = conn.createStatement()) {
                String createUserSql = "CREATE USER IF NOT EXISTS '" + login +
                        "'@'%' IDENTIFIED BY '" + usuario.getSenha() + "'";
                createUserStmt.executeUpdate(createUserSql);

                switch (usuario.getTipo()) {
                    case ALUNO:
                        createUserStmt.executeUpdate("GRANT rl_aluno TO '" + login + "'@'%'");
                        createUserStmt.executeUpdate("SET DEFAULT ROLE rl_aluno TO '" + login + "'@'%'");
                        break;

                    case ESTAGIARIO:
                        createUserStmt.executeUpdate("GRANT rl_estagiario TO '" + login + "'@'%'");
                        createUserStmt.executeUpdate("SET DEFAULT ROLE rl_estagiario TO '" + login + "'@'%'");
                        break;

                    case BIBLIOTECARIO:
                        createUserStmt.executeUpdate("GRANT rl_bibliotecario TO '" + login + "'@'%'");
                        createUserStmt.executeUpdate("SET DEFAULT ROLE rl_bibliotecario TO '" + login + "'@'%'");
                        break;

                    case GERENTE:
                        createUserStmt.executeUpdate("GRANT rl_gerente TO '" + login + "'@'%'");
                        createUserStmt.executeUpdate("SET DEFAULT ROLE rl_gerente TO '" + login + "'@'%'");
                        break;
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1370 || e.getErrorCode() == 0) {
                JOptionPane.showMessageDialog(null,
                        "Você não possui permissão para cadastrar usuário.",
                        "Acesso Negado",
                        JOptionPane.ERROR_MESSAGE);

            } else if (e.getErrorCode() == 1644) {
                String msg = e.getMessage().replace("SQLSTATE 45000: ", "");
                JOptionPane.showMessageDialog(null, msg, "Erro de Regra", JOptionPane.WARNING_MESSAGE);

            } else {
                JOptionPane.showMessageDialog(null,
                        "Ocorreu um erro inesperado: " + e.getMessage() + e.getErrorCode(),
                        "Erro no Sistema",
                        JOptionPane.ERROR_MESSAGE);
            }
            throw e;
        }
    }
}
