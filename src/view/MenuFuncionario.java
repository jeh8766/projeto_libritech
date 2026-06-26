package view;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import dao.BackupDAO;
import dao.EmprestimoDAO;
import dao.LivroDAO;
import dao.UsuarioDAO;
import model.Endereco;
import model.Livro;
import model.Login;
import model.Usuario;
import model.enums.TipoUsuario;

public class MenuFuncionario {

    public static void menuFuncionario() {
        boolean opcao = true;
        do {
            String[] options = { "Ver Livros", "Cadastrar livro", "Realizar Empréstimo", "Renovar Empréstimo",
                    "Realizar devolução", "Excluir livro", "Gerar Backup", "Cadastrar usuario", "Sair" };
            int escolha = JOptionPane.showOptionDialog(null, "Menu do Funcionário", "LibriTech",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
            switch (escolha) {
                case 0:
                    listarTodosOsLivros();
                    break;
                case 1:
                    cadastrarLivro();
                    break;
                case 2:
                    efetuarEmprestimo();
                    break;
                case 3:
                    renovarEmprestimo();
                    break;
                case 4:
                    realizarDevolucao();
                    break;
                case 5:
                    excluirLivro();
                    break;
                case 6:
                    realizarBackup();
                    break;
                case 7:
                    cadastrarUsuario();
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Encerrando...");
                    opcao = false;
                    break;
            }
        } while (opcao == true);
    }

    private static void listarTodosOsLivros() {
        LivroDAO dao = new LivroDAO();
        try {
            List<Livro> lista = dao.buscarTodosOsLivros();

            StringBuilder sb = new StringBuilder("--- ACESSO AO ESTOQUE COMPLETO ---\n");
            sb.append(" ID | Título | Autor | Preço Custo | Qtd | Status \n");
            sb.append("-------------------------------------------------------------------------------\n");

            for (Livro l : lista) {
                sb.append(l.getIdLivro()).append(" | ")
                        .append(l.getTitulo()).append(" | ")
                        .append(l.getAutor()).append(" | ")
                        .append("R$ ").append(l.getPrecoCusto()).append(" | ")
                        .append(l.getQuantidadeEstoque()).append(" | ")
                        .append(l.getStatus()).append("\n");
            }

            JOptionPane.showMessageDialog(null, sb.toString(), "Gestão de Livros",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
        }
    }

    private static void efetuarEmprestimo() {
        try {
            String idU = JOptionPane.showInputDialog("ID do Usuário:");
            String idL = JOptionPane.showInputDialog("ID do Livro:");

            if (idU != null && idL != null) {
                EmprestimoDAO dao = new EmprestimoDAO();
                dao.registrarEmprestimo(Integer.parseInt(idU), Integer.parseInt(idL));

                JOptionPane.showMessageDialog(null, "Empréstimo realizado com sucesso!");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "IDs devem ser números.");
        } catch (SQLException e) {
        }
    }

    private static void cadastrarUsuario() {
        try {
            String nome = JOptionPane.showInputDialog("Nome:");
            String cpf = JOptionPane.showInputDialog("CPF:");
            String email = JOptionPane.showInputDialog("E-mail:");
            String senha = JOptionPane.showInputDialog("Senha:");

            String[] tipos = { "ALUNO", "ESTAGIARIO", "BIBLIOTECARIO", "GERENTE" };
            String tipoSelecionado = (String) JOptionPane.showInputDialog(null, "Tipo de Usuário:",
                    "Cadastro", JOptionPane.QUESTION_MESSAGE, null, tipos, tipos[0]);

            String logradouro = JOptionPane.showInputDialog("Logradouro:");
            String bairro = JOptionPane.showInputDialog("Bairro:");
            String cidade = JOptionPane.showInputDialog("Cidade:");
            String uf = JOptionPane.showInputDialog("UF:");

            Usuario usuario = new Usuario(nome, cpf, email, senha, TipoUsuario.valueOf(tipoSelecionado));
            Endereco endereco = new Endereco(logradouro, bairro, cidade, uf, usuario.getIdUsuario());

            UsuarioDAO dao = new UsuarioDAO();
            dao.cadastrarUsuarioCompleto(usuario, endereco);

            JOptionPane.showMessageDialog(null, "Usuário cadastrado com sucesso e permissões aplicadas no banco!");

        } catch (Exception e) {
        }
    }

    private static void cadastrarLivro() {
        try {
            String titulo = JOptionPane.showInputDialog("Título do Livro:");
            String autor = JOptionPane.showInputDialog("Autor:");
            String isbn = JOptionPane.showInputDialog("ISBN:");
            String precoStr = JOptionPane.showInputDialog("Preço de Custo:");
            String qtdStr = JOptionPane.showInputDialog("Quantidade em Estoque:");

            Livro novoLivro = new Livro();
            novoLivro.setTitulo(titulo);
            novoLivro.setAutor(autor);
            novoLivro.setIsbn(isbn);
            novoLivro.setPrecoCusto(new BigDecimal(precoStr));
            novoLivro.setQuantidadeEstoque(Integer.parseInt(qtdStr));

            LivroDAO dao = new LivroDAO();
            dao.cadastrarLivro(novoLivro);

            JOptionPane.showMessageDialog(null, "Livro cadastrado com sucesso!");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Erro: Preço ou Quantidade em formato inválido.");
        } catch (SQLException e) {
        }
    }

    private static void renovarEmprestimo() {
        String input = JOptionPane.showInputDialog("Digite o ID do Empréstimo para renovar:");
        if (input == null)
            return;

        try {
            int idEmprestimo = Integer.parseInt(input);
            EmprestimoDAO dao = new EmprestimoDAO();

            dao.renovarEmprestimo(idEmprestimo);

            JOptionPane.showMessageDialog(null, "Empréstimo renovado com sucesso por mais 7 dias!");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID inválido. Por favor, digite apenas números.");
        } catch (SQLException e) {
        }
    }

    private static void realizarDevolucao() {
        String input = JOptionPane.showInputDialog("Digite o ID do Empréstimo para devolver:");
        if (input == null)
            return;

        try {
            int idEmprestimo = Integer.parseInt(input);
            EmprestimoDAO dao = new EmprestimoDAO();

            BigDecimal multa = dao.realizarDevolucao(idEmprestimo);

            if (multa.compareTo(BigDecimal.ZERO) > 0) {
                JOptionPane.showMessageDialog(null, "Devolução realizada com atraso!\nMulta pendente: R$ " + multa);
            } else {
                JOptionPane.showMessageDialog(null, "Devolução realizada com sucesso dentro do prazo!");
            }

        } catch (SQLException e) {
        }
    }

    private static void excluirLivro() {
        String input = JOptionPane.showInputDialog("Digite o ID do livro que deseja excluir:");
        if (input == null)
            return;

        try {
            int idLivro = Integer.parseInt(input);

            int confirmacao = JOptionPane.showConfirmDialog(null,
                    "Tem certeza que deseja excluir o livro ID " + idLivro + "?",
                    "Confirmar Exclusão", JOptionPane.YES_NO_OPTION);

            if (confirmacao == JOptionPane.YES_OPTION) {
                LivroDAO dao = new LivroDAO();
                dao.excluirLivro(idLivro);
                JOptionPane.showMessageDialog(null, "Livro excluído com sucesso!");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "ID inválido.");
        } catch (SQLException e) {
        }
    }

    private static void realizarBackup() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Escolha onde salvar o backup");

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String caminho = fileToSave.getAbsolutePath() + ".sql";

            try {
                BackupDAO backup = new BackupDAO();
                backup.gerarBackup(caminho, Login.usuarioLogado, Login.senhaLogada);
            } catch (Exception e) {
            }
        }
    }

}
