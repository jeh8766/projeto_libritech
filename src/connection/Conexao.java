package connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    private static final String URL = "jdbc:mysql://localhost:3306/db_libritech";

    private static Connection conexaoAtual;

    public static Connection conectar(String usuario, String senha) throws SQLException {

        conexaoAtual = DriverManager.getConnection(URL, usuario, senha);
        return conexaoAtual;
    }

    public static Connection getConexaoAtual() {
        return conexaoAtual;
    }
}
