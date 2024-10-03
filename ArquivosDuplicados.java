import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

public class ArquivosDuplicados extends JFrame {
    private JTextField directoryPathField;
    private JTextArea resultArea;
    private JButton searchButton;
    private JButton deleteButton;
    private File selectedDirectory;

    public ArquivosDuplicados() {
        setTitle("Busca de Arquivos Duplicados");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Painel superior com entrada de pasta e botão de pesquisa
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());

        directoryPathField = new JTextField(30);
        JButton browseButton = new JButton("Pesquisar...");
        browseButton.addActionListener(new BrowseAction());

        topPanel.add(new JLabel("Pasta:"));
        topPanel.add(directoryPathField);
        topPanel.add(browseButton);

        // Painel central com exibição do relatório
        resultArea = new JTextArea(15, 50);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // Painel inferior com botões de busca e exclusão
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());

        searchButton = new JButton("Iniciar Busca");
        searchButton.addActionListener(new SearchAction());

        deleteButton = new JButton("Excluir Arquivos Duplicados");
        deleteButton.setEnabled(false);  // Habilitado apenas após a busca
        deleteButton.addActionListener(new DeleteAction());

        bottomPanel.add(searchButton); // Adiciona o botão de busca no painel inferior
        bottomPanel.add(deleteButton); // Adiciona o botão de deletar no painel inferior

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Ação para escolher o diretório com JFileChooser
    private class BrowseAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
            chooser.setDialogTitle("Escolha a pasta para pesquisar");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = chooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                selectedDirectory = chooser.getSelectedFile();
                directoryPathField.setText(selectedDirectory.getAbsolutePath());
            }
        }
    }

    // Ação para iniciar a busca de arquivos duplicados
    private class SearchAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedDirectory == null || !selectedDirectory.isDirectory()) {
                JOptionPane.showMessageDialog(null, "Por favor, selecione um diretório válido.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            resultArea.setText("");  // Limpa a área de resultado
            resultArea.append("Psta da busca atual: "+ selectedDirectory.getAbsolutePath() + "\n");
            resultArea.append("Iniciando busca de arquivos duplicados com numeração...\n");

            Map<String, java.util.List<File>> duplicates = findDuplicateFilesWithNumber(selectedDirectory);

            if (duplicates.isEmpty()) {
                resultArea.append("Nenhum arquivo duplicado com numeração foi encontrado.");
                deleteButton.setEnabled(false);  // Desabilita o botão de excluir se não houver duplicados
            } else {
                resultArea.append("Arquivos duplicados com numeração encontrados:\n\n");
                for (Map.Entry<String, java.util.List<File>> entry : duplicates.entrySet()) {
                    resultArea.append("Arquivo: " + entry.getValue().get(0).getName() + "\n");
                    for (File file : entry.getValue()) {
                        resultArea.append(" - " + file.getAbsolutePath() + "\n");
                    }
                    resultArea.append("\n");
                }
                deleteButton.setEnabled(true);  // Habilita o botão de deletar
            }
        }
    }

    // Ação para excluir os arquivos duplicados
    private class DeleteAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Map<String, java.util.List<File>> duplicates = findDuplicateFilesWithNumber(selectedDirectory);
            int totalDeleted = 0;
            long totalSizeDeleted = 0;  // Variável para armazenar o tamanho total excluído

            for (Map.Entry<String, java.util.List<File>> entry : duplicates.entrySet()) {
                java.util.List<File> files = entry.getValue();
                // Mantém o primeiro arquivo, exclui os outros que têm numeração
                for (int i = 1; i < files.size(); i++) {
                    File fileToDelete = files.get(i);
                    long fileSize = fileToDelete.length();  // Tamanho do arquivo antes de excluir
                    if (fileToDelete.delete()) {
                        totalDeleted++;
                        totalSizeDeleted += fileSize;  // Adiciona o tamanho do arquivo excluído
                    }
                }
            }

            saveReport(duplicates, totalDeleted);

            // Exibe o relatório final
            resultArea.append("\n\n--== Relatório Geral ==--\n\n");
            resultArea.append("Quantidade de arquivos excluídos: " + totalDeleted + "\n");
            resultArea.append("Quantidade de espaço liberado: " + (1024 / (totalSizeDeleted / (1024.0 * 1024.0))) + " MB\n");  // Converte para MB
            resultArea.append("Caminho completo do arquivo de relatório gerado: " + new File(selectedDirectory, "resumo.txt").getAbsolutePath() + "\n");

            deleteButton.setEnabled(false);  // Desabilita o botão após exclusão
        }
    }

    // Função para buscar arquivos duplicados que têm numeração no nome
    private Map<String, java.util.List<File>> findDuplicateFilesWithNumber(File directory) {
        Map<String, java.util.List<File>> fileMap = new HashMap<>();
        Map<String, java.util.List<File>> duplicates = new HashMap<>();
        Pattern pattern = Pattern.compile("^(.*)\\s(\\d+)\\.(.*)$");  // Regex para pegar arquivos com numeração antes da extensão

        try {
            Files.walk(directory.toPath())
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            File file = path.toFile();
                            Matcher matcher = pattern.matcher(file.getName());

                            if (matcher.matches()) {  // Encontra arquivos com numeração no nome
                                String baseName = matcher.group(1) + "." + matcher.group(3);  // Nome base do arquivo sem numeração
                                String hash = calculateFileHash(file);

                                // Verifica duplicidade com base no nome e no hash
                                if (!fileMap.containsKey(baseName)) {
                                    fileMap.put(baseName, new ArrayList<>());
                                }
                                fileMap.get(baseName).add(file);
                            }
                        } catch (IOException | NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Identifica duplicatas
        for (Map.Entry<String, java.util.List<File>> entry : fileMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicates.put(entry.getKey(), entry.getValue());
            }
        }

        return duplicates;
    }

    // Função para calcular o hash do arquivo
    private String calculateFileHash(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;

            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }

        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Função para salvar o relatório de duplicados
    private void saveReport(Map<String, java.util.List<File>> duplicates, int totalDeleted) {
        File reportFile = new File(selectedDirectory, "resumo.txt");
        try (PrintWriter writer = new PrintWriter(reportFile)) {
            writer.println("Relatório de arquivos duplicados:");
            writer.println("=================================\n");

            for (Map.Entry<String, java.util.List<File>> entry : duplicates.entrySet()) {
                writer.println("Arquivo: " + entry.getValue().get(0).getName());
                for (File file : entry.getValue()) {
                    writer.println(" - " + file.getAbsolutePath());
                }
                writer.println();
            }

            writer.println("Total de arquivos excluídos: " + totalDeleted);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        JOptionPane.showMessageDialog(null, "Relatório salvo em " + reportFile.getAbsolutePath(), "Relatório Gerado", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ArquivosDuplicados app = new ArquivosDuplicados();
            app.setVisible(true);
        });
    }
}