package model;

import java.sql.SQLException;

public class Login {
    public static int idUsuarioLogado;
    public static String usuarioLogado;
    public static String senhaLogada;

    public static void realizarLogin(String email, String senha) throws SQLException {
        String sql = "{CALL sp_obter_id_usuario(?)}";
        java.sql.Connection conn = connection.Conexao.getConexaoAtual();

        try (java.sql.CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setString(1, email);

            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    idUsuarioLogado = rs.getInt("id_usuario");
                    usuarioLogado = email;
                    senhaLogada = senha;
                } else {
                    throw new SQLException("E-mail não encontrado na base de dados.");
                }
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    public static String getUsuarioLogado() {
        return usuarioLogado;
    }

    public static void setUsuarioLogado(String usuarioLogado) {
        Login.usuarioLogado = usuarioLogado;
    }

    public static String getSenhaLogada() {
        return senhaLogada;
    }

    public static void setSenhaLogada(String senhaLogada) {
        Login.senhaLogada = senhaLogada;
    }

    public static int getIdUsuarioLogado() {
        return idUsuarioLogado;
    }

    public static void setIdUsuarioLogado(int idUsuarioLogado) {
        Login.idUsuarioLogado = idUsuarioLogado;
    }

}
