import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class TagExtractorGUI extends JFrame {
    private final JTextArea tagTextArea;
    private final JFileChooser fileChooser;

    private JLabel fileNameLabel;
    private final Map<String, Integer> tagFrequencyMap;
    private final Set<String> stopWordsSet;

    public TagExtractorGUI() {
        setTitle("Tag Extractor");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        fileNameLabel = new JLabel("No file selected");
        tagTextArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(tagTextArea);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(fileNameLabel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JButton openButton = new JButton("Open File");
        openButton.addActionListener(new OpenFileListener());
        add(openButton, BorderLayout.NORTH);

        JButton saveButton = new JButton("Save Tags");
        saveButton.addActionListener(new SaveTagsListener());
        add(saveButton, BorderLayout.SOUTH);

        fileChooser = new JFileChooser();
        tagFrequencyMap = new HashMap<>();
        stopWordsSet = loadStopWords("src/EnglishStopWords.txt");
    }

    private Set<String> loadStopWords(String fileName) {
        Set<String> stopWords = new TreeSet<>();
        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextLine()) {
                stopWords.add(scanner.nextLine().toLowerCase());
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Stop words file not found: " + fileName,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return stopWords;
    }

    private void processFile(File file) {
        tagFrequencyMap.clear();
        tagTextArea.setText("");

        fileNameLabel.setText("Extracting from: " + file.getName());

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().toLowerCase();
                String[] words = line.split("\\s+"); // Split by whitespace
                for (String word : words) {
                    word = word.replaceAll("[^a-zA-Z]", ""); // Remove non-letter characters
                    if (!word.isEmpty() && !stopWordsSet.contains(word)) { // Check for non-empty word
                        tagFrequencyMap.put(word, tagFrequencyMap.getOrDefault(word, 0) + 1);
                    }
                }
            }

            // Display tags and frequencies
            for (Map.Entry<String, Integer> entry : tagFrequencyMap.entrySet()) {
                tagTextArea.append(entry.getKey() + ": " + entry.getValue() + "\n");
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "File not found: " + file.getName(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class OpenFileListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int result = fileChooser.showOpenDialog(TagExtractorGUI.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                processFile(selectedFile);
            }
        }
    }

    private class SaveTagsListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser saveFileChooser = new JFileChooser();
            int result = saveFileChooser.showSaveDialog(TagExtractorGUI.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File outputFile = saveFileChooser.getSelectedFile();
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(outputFile);
                    for (Map.Entry<String, Integer> entry : tagFrequencyMap.entrySet()) {
                        writer.println(entry.getKey() + " " + entry.getValue());
                    }
                } catch (FileNotFoundException ex) {
                    JOptionPane.showMessageDialog(TagExtractorGUI.this, "Error saving tags to file",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return; // Exit the listener if saving fails
                } finally {
                    if (writer != null) {
                        writer.close(); // Ensure the writer is closed
                    }
                }

                // Exit the program after the file is saved
                System.exit(0);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TagExtractorGUI tagExtractorGUI = new TagExtractorGUI();
            tagExtractorGUI.setVisible(true);
        });
    }
}
