package model;

public class Login {
    public static String usuarioLogado;
    public static String senhaLogada;

    public static void realizarLogin(String user, String pass) {
        usuarioLogado = user;
        senhaLogada = pass;
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

}
