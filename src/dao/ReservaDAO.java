package dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import connection.Conexao;
import model.Login;

public class ReservaDAO {

    public void registrarReserva(int idLivro) throws SQLException {
        String sql = "{CALL sp_reservar_livro(?, ?)}";

        Connection conn = Conexao.getConexaoAtual();

        try (CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, Login.idUsuarioLogado);
            stmt.setInt(2, idLivro);
            stmt.execute();

        } catch (SQLException e) {
            {
                throw e;
            }
        }
    }
}
