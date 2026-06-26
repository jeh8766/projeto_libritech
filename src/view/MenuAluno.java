package view;

import java.sql.SQLException;
import java.util.List;

import javax.swing.JOptionPane;

import dao.EmprestimoDAO;
import dao.LivroDAO;
import model.Emprestimo;
import model.Livro;

public class MenuAluno {

    public static void menuAluno() {
        boolean opcao = true;
        do {
            String[] options = { "Consultar Acervo Público", "Meus empréstimos", "Sair" };
            int escolha = JOptionPane.showOptionDialog(null, "Menu do Aluno", "LibriTech",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
            switch (escolha) {
                case 0:
                    buscarAcervo();
                    break;
                case 1:
                    meusEmprestimos();
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Encerrando...");
                    opcao = false;
                    break;
            }
        } while (opcao == true);
    }

    private static void buscarAcervo() {
        try {
            LivroDAO dao = new LivroDAO();

            List<Livro> lista = dao.buscarAcervoPublico();
            String texto = "Livros:\n";
            for (Livro l : lista) {
                texto += l.getTitulo() + " - " + l.getAutor() + "\n";
            }
            JOptionPane.showMessageDialog(null, texto);
        } catch (SQLException e) {

        }
    }

    private static void meusEmprestimos() {
        EmprestimoDAO dao = new EmprestimoDAO();

        try {
            List<Emprestimo> emprestimos = dao.buscarMeusEmprestimos();

            if (emprestimos.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Você ainda não possui empréstimos registrados.");
                return;
            }

            StringBuilder texto = new StringBuilder("--- Histórico de Empréstimos ---\n\n");
            for (Emprestimo e : emprestimos) {
                texto.append("Título: ").append(e.getTituloLivro())
                        .append(" | Data: ").append(e.getDataSaida().toLocalDate())
                        .append("\n");
            }

            JOptionPane.showMessageDialog(null, texto.toString());

        } catch (SQLException e) {
        }
    }
}
