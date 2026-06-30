package view;

import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import connection.Conexao;
import model.Login;

public class SistemaBiblioteca {

    public static void iniciarSistema() {
        String[] opcoes = { "Funcionário", "Aluno", "Sair" };
        int escolha = JOptionPane.showOptionDialog(null,
                "Qual é o seu perfil de acesso?",
                "LibriTech - Início",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoes,
                opcoes[0]);

        if (escolha == 2 || escolha == -1) {
            System.exit(0);
        }

        String usuarioDB = JOptionPane.showInputDialog(null,
                "Usuário do Banco (ex: user@email.com):",
                "Login", JOptionPane.QUESTION_MESSAGE);

        if (usuarioDB == null) {
            System.exit(0);
        }

        JPasswordField pf = new JPasswordField();
        int okCxl = JOptionPane.showConfirmDialog(null, pf,
                "Senha do Banco:",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (okCxl != JOptionPane.OK_OPTION) {
            System.exit(0);
        }

        String senhaDB = new String(pf.getPassword());

        try {

            Conexao.conectar(usuarioDB, senhaDB);
            Login.realizarLogin(usuarioDB, senhaDB);
            JOptionPane.showMessageDialog(null,
                    "Bem-vindo ao LibriTech.",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            if (escolha == 0) {
                MenuFuncionario.menuFuncionario();
            } else if (escolha == 1) {
                MenuAluno.menuAluno();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Erro de autenticação: Credenciais inválidas.\n",
                    "Erro de Login", JOptionPane.ERROR_MESSAGE);

            iniciarSistema();
        }
    }

}
