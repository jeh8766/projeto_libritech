package dao;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import connection.Conexao;
import model.Emprestimo;

public class EmprestimoDAO {

    public void registrarEmprestimo(int idUsuario, int idLivro) throws SQLException {
        String sql = "{CALL sp_transacao_emprestimo(?, ?)}";

        Connection conn = Conexao.getConexaoAtual();

        if (conn == null) {
            throw new SQLException("Conexão não encontrada. Faça o login primeiro.");
        }

        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idLivro);

            stmt.execute();
        } catch (SQLException e) {
            try {
                tratarErroSQL(e);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw e;
        }
    }

    public List<Emprestimo> buscarMeusEmprestimos() throws SQLException {
        List<Emprestimo> lista = new ArrayList<>();
        String sqlProcedure = "{CALL sp_meus_emprestimos()}";

        Connection conn = Conexao.getConexaoAtual();

        try (CallableStatement stmt = conn.prepareCall(sqlProcedure);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Emprestimo e = new Emprestimo();
                e.setIdEmprestimo(rs.getInt("id_emprestimo"));
                e.setTituloLivro(rs.getString("livro"));
                e.setDataSaida(rs.getObject("data_saida", LocalDateTime.class));
                lista.add(e);
            }
        } catch (SQLException e) {
            try {
                tratarErroSQL(e);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw e;
        }
        return lista;
    }

    public void renovarEmprestimo(int idEmprestimo) throws SQLException {
        String sql = "{CALL sp_renovar_emprestimo(?)}";

        Connection conn = Conexao.getConexaoAtual();
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, idEmprestimo);
            stmt.execute();
        } catch (SQLException e) {
            try {
                tratarErroSQL(e);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw e;
        }
    }

    public BigDecimal realizarDevolucao(int idEmprestimo) throws SQLException {
        String sql = "{CALL sp_transacao_devolucao(?, ?)}";
        BigDecimal multaGerada = BigDecimal.ZERO;

        Connection conn = Conexao.getConexaoAtual();
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, idEmprestimo);
            stmt.registerOutParameter(2, java.sql.Types.DECIMAL);

            stmt.execute();

            multaGerada = stmt.getBigDecimal(2);
        } catch (SQLException e) {
            try {
                tratarErroSQL(e);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw e;
        }
        return multaGerada;
    }

    private void tratarErroSQL(SQLException e) throws SQLException {
        String mensagemAmigavel;

        if (e.getErrorCode() == 1370 || e.getErrorCode() == 0) {
            mensagemAmigavel = "Acesso Negado: Você não possui permissão para realizar esta operação.";
        } else if (e.getErrorCode() == 1644) {
            mensagemAmigavel = e.getMessage().replace("SQLSTATE 45000: ", "");
        } else {
            mensagemAmigavel = "Erro inesperado no sistema: " + e.getMessage();
        }

        javax.swing.JOptionPane.showMessageDialog(null, mensagemAmigavel, "Aviso do Sistema",
                e.getErrorCode() == 1644 ? javax.swing.JOptionPane.WARNING_MESSAGE
                        : javax.swing.JOptionPane.ERROR_MESSAGE);

    }
}
