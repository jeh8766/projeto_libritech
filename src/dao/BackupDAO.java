package dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class BackupDAO {
    public void gerarBackup(String caminhoDestino, String usuario, String senha)
            throws IOException, InterruptedException {

        String mysqldumpPath = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe";
        String dbName = "db_libritech";

        List<String> comando = new ArrayList<>();
        comando.add(mysqldumpPath);
        comando.add("-u" + usuario);
        comando.add("-p" + senha);
        comando.add(dbName);
        comando.add("-r" + caminhoDestino);

        try {
            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.redirectErrorStream(true);
            Process processo = pb.start();

            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(processo.getInputStream()))) {

                String line;
                StringBuilder erroCompleto = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    erroCompleto.append(line).append("\n");
                }

                int resultado = processo.waitFor();

                if (resultado != 0) {
                    String mensagemErro = erroCompleto.toString();

                    if (mensagemErro.contains("Access denied") || mensagemErro.contains("1044")) {
                        throw new Exception(
                                "Você não tem permissão de administrador para realizar o backup do banco de dados.");
                    } else {
                        throw new Exception("Falha no Backup (Código " + resultado + "): " + mensagemErro);
                    }
                }
                JOptionPane.showMessageDialog(null, "Backup realizado com sucesso!");
            }
        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Erro de E/S: Verifique o caminho de destino ou se o mysqldump.exe existe.\n" + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            JOptionPane.showMessageDialog(null, "O processo de backup foi interrompido.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Falha ao gerar backup:\n" + e.getMessage());
        }
    }

}
