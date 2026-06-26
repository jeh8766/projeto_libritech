package dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import connection.Conexao;
import model.Livro;
import model.enums.StatusLivro;

public class LivroDAO {

    public List<Livro> buscarAcervoPublico() throws SQLException {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT titulo, autor, isbn, status FROM vw_acervo_publico";

        try (PreparedStatement stmt = Conexao.getConexaoAtual().prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Livro livro = new Livro(
                        null,
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getString("isbn"),
                        null,
                        0,
                        StatusLivro.valueOf(rs.getString("status")));
                livros.add(livro);
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1370 || e.getErrorCode() == 0 || e.getErrorCode() == 1142) {
                JOptionPane.showMessageDialog(null,
                        "Você não possui permissão para visualizar o acervo público.",
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
        return livros;
    }

    public List<Livro> buscarTodosOsLivros() throws SQLException {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT * FROM db_libritech.livros";
        try (PreparedStatement stmt = Conexao.getConexaoAtual().prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                livros.add(new Livro(
                        rs.getInt("id_livro"),
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getString("isbn"),
                        rs.getBigDecimal("preco_custo"),
                        rs.getInt("quantidade_estoque"),
                        StatusLivro.valueOf(rs.getString("status"))));
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1370 || e.getErrorCode() == 0) {
                JOptionPane.showMessageDialog(null,
                        "Você não possui permissão para visualizar todos os livros.",
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
        return livros;
    }

    public void cadastrarLivro(Livro livro) throws SQLException {
        String sql = "{CALL sp_cadastrar_livro(?, ?, ?, ?, ?)}";
        Connection conn = Conexao.getConexaoAtual();

        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, livro.getTitulo());
            stmt.setString(2, livro.getAutor());
            stmt.setString(3, livro.getIsbn());
            stmt.setBigDecimal(4, livro.getPrecoCusto());
            stmt.setInt(5, livro.getQuantidadeEstoque());
            stmt.execute();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1370 || e.getErrorCode() == 0) {
                JOptionPane.showMessageDialog(null,
                        "Você não possui permissão para cadastrar           j livros.",
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

    public void excluirLivro(int idLivro) throws SQLException {
        String sql = "{CALL sp_excluir_livro(?)}";

        try (Connection conn = Conexao.getConexaoAtual();
                CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, idLivro);
            stmt.execute();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1370 || e.getErrorCode() == 0) {
                JOptionPane.showMessageDialog(null,
                        "Você não possui permissão para excluir livros.",
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