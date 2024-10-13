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
    private JProgressBar progressBar;  // Barra de progresso
    private JLabel progressLabel;  // Rótulo para exibir o status
    private File selectedDirectory;

    public ArquivosDuplicados() {
        setTitle("Busca de Arquivos Duplicados");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Painel superior com entrada de pasta, barra de progresso e botão de pesquisa
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Campo de texto para o caminho da pasta
        directoryPathField = new JTextField(30);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        topPanel.add(directoryPathField, gbc);
        
        // Botão para escolher o diretório
        JButton browseButton = new JButton("Pesquisar...");
        browseButton.addActionListener(new BrowseAction());
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        topPanel.add(browseButton, gbc);

        // Barra de progresso e rótulo de status
        progressLabel = new JLabel("Status: ");  // Rótulo que exibe o status
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        topPanel.add(progressLabel, gbc);

        progressBar = new JProgressBar(0, 100);  // Barra de progresso de 0 a 100
        progressBar.setStringPainted(true);  // Exibe o valor da barra de progresso como texto
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        topPanel.add(progressBar, gbc);

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

        // Adiciona o painel superior, a área de resultado e o painel inferior
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
            resultArea.append("Pasta da busca atual: " + selectedDirectory.getAbsolutePath() + "\n");
            resultArea.append("Iniciando busca de arquivos duplicados com numeração...\n");

            // Atualiza a barra de progresso e o rótulo de status
            progressLabel.setText("Status: Buscando arquivos duplicados...");
            progressBar.setValue(0);  // Reseta a barra de progresso

            // Inicia a busca em uma nova thread
            SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Map<String, java.util.List<File>> duplicates = findDuplicateFilesWithNumber(selectedDirectory);

                    if (duplicates.isEmpty()) {
                        resultArea.append("Nenhum arquivo duplicado com numeração foi encontrado.");
                        deleteButton.setEnabled(false);  // Desabilita o botão de excluir se não houver duplicados
                    } else {
                        resultArea.append("Arquivos duplicados com numeração encontrados:\n\n");
                        int totalFiles = duplicates.size();
                        int processedFiles = 0;

                        for (Map.Entry<String, java.util.List<File>> entry : duplicates.entrySet()) {
                            resultArea.append("Arquivo: " + entry.getValue().get(0).getName() + "\n");
                            for (File file : entry.getValue()) {
                                resultArea.append(" - " + file.getAbsolutePath() + "\n");
                            }
                            resultArea.append("\n");

                            // Atualiza a barra de progresso e o status
                            processedFiles++;
                            int progress = (int) ((processedFiles / (double) totalFiles) * 100);
                            publish(progress);  // Envia o valor para a barra de progresso
                        }
                        deleteButton.setEnabled(true);  // Habilita o botão de deletar
                    }
                    return null;
                }

                @Override
                protected void process(java.util.List<Integer> chunks) {
                    // Atualiza a barra de progresso
                    for (int progress : chunks) {
                        progressBar.setValue(progress);
                    }
                }

                @Override
                protected void done() {
                    progressLabel.setText("Status: Busca concluída.");
                    progressBar.setValue(100);  // Busca concluída
                }
            };
            worker.execute();
        }
    }

    // Ação para excluir os arquivos duplicados
    private class DeleteAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            progressLabel.setText("Status: Excluindo arquivos duplicados...");
            progressBar.setValue(0);  // Reseta a barra de progresso

            SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Map<String, java.util.List<File>> duplicates = findDuplicateFilesWithNumber(selectedDirectory);
                    int totalDeleted = 0;
                    long totalSizeDeleted = 0;  // Variável para armazenar o tamanho total excluído

                    int totalFiles = duplicates.size();
                    int processedFiles = 0;

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
                        processedFiles++;
                        int progress = (int) ((processedFiles / (double) totalFiles) * 100);
                        publish(progress);  // Envia o valor para a barra de progresso
                    }

                    saveReport(duplicates, totalDeleted);

                    // Exibe o relatório final
                    resultArea.append("\n\n--== Relatório Geral ==--\n\n");
                    resultArea.append("Quantidade de arquivos excluídos: " + totalDeleted + "\n");
                    // Limita o espaço liberado a duas casas decimais
                    double totalSizeInMB = totalSizeDeleted / (1024.0 * 1024.0);  // Converte o tamanho total para MB
                    resultArea.append(String.format("Quantidade de espaço liberado: %.2f MB\n", totalSizeInMB));
                    resultArea.append("Caminho completo do arquivo de relatório gerado: " + new File(selectedDirectory, "resumo.txt").getAbsolutePath() + "\n");

                    return null;
                }

                @Override
                protected void process(java.util.List<Integer> chunks) {
                    // Atualiza a barra de progresso
                    for (int progress : chunks) {
                        progressBar.setValue(progress);
                    }
                }

                @Override
                protected void done() {
                    progressLabel.setText("Status: Exclusão concluída.");
                    progressBar.setValue(100);  // Exclusão concluída
                    deleteButton.setEnabled(false);  // Desabilita o botão após exclusão
                }
            };
            worker.execute();
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