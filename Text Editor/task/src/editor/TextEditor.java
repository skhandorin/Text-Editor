package editor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class TextEditor extends JFrame {

    JFileChooser fileChooser = new JFileChooser();
    JPanel topPanel = new JPanel();
    FlowLayout topPanelLayout = new FlowLayout();
    JTextField searchTextField = new JTextField();
    JTextArea textArea = new JTextArea();
    private JCheckBox useRegexCheckBox = new JCheckBox();
    private String filename;
    private TextSearcher textSearcher;

    public TextEditor() {
        super("The first stage");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        initMenu();
        initComponents();

        setVisible(true);
        textArea.grabFocus();
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setName("MenuFile");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.setName("MenuOpen");
        openMenuItem.addActionListener(this::openButtonAction);

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setName("MenuSave");
        saveMenuItem.addActionListener(this::saveButtonAction);

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setName("MenuExit");
        exitMenuItem.addActionListener(e -> dispose());

        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);

        JMenu searchMenu = new JMenu("Search");
        searchMenu.setName("MenuSearch");
        searchMenu.setMnemonic(KeyEvent.VK_S);

        JMenuItem startSearchMenuItem = new JMenuItem("Start search");
        startSearchMenuItem.setName("MenuStartSearch");
        startSearchMenuItem.addActionListener(this::searchButtonAction);

        JMenuItem previousMatchMenuItem = new JMenuItem("Previous match");
        previousMatchMenuItem.setName("MenuPreviousMatch");
        previousMatchMenuItem.addActionListener(this::previousButtonAction);

        JMenuItem nextMatchMenuItem = new JMenuItem("Next match");
        nextMatchMenuItem.setName("MenuNextMatch");
        nextMatchMenuItem.addActionListener(this::nextButtonAction);

        JMenuItem useRegExpMenuItem = new JMenuItem("Use regular expressions");
        useRegExpMenuItem.setName("MenuUseRegExp");
        useRegExpMenuItem.addActionListener(e -> useRegexCheckBox.setSelected(!useRegexCheckBox.isSelected()));

        searchMenu.add(startSearchMenuItem);
        searchMenu.add(previousMatchMenuItem);
        searchMenu.add(nextMatchMenuItem);
        searchMenu.add(useRegExpMenuItem);
        menuBar.add(searchMenu);

        setJMenuBar(menuBar);
    }

    private void initComponents() {
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setAlignment(FlowLayout.LEFT);
        topPanel.setBorder(BorderFactory
                .createCompoundBorder(new EmptyBorder(10, 10, 10, 10),
                        new EmptyBorder( 0, 0, 0, 0)));

        JPanel textAreaPanel = new JPanel();
        textAreaPanel.setLayout(new BorderLayout());
        textAreaPanel.setBorder(BorderFactory
                .createCompoundBorder(new EmptyBorder(0, 15, 15, 15),
                        new EmptyBorder(0, 0, 0, 0)));

        initTopPanel(topPanel);
        initTextAreaPanel(textAreaPanel);

        add(topPanel, BorderLayout.NORTH);
        add(textAreaPanel, BorderLayout.CENTER);

        fileChooser.setVisible(false);
        fileChooser.setName("FileChooser");
        add(fileChooser, BorderLayout.SOUTH);
    }

    private void initTextAreaPanel(JPanel textAreaPanel) {
        textArea.setName("TextArea");

        JScrollPane scrollableTextArea = new JScrollPane(textArea,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollableTextArea.setName("ScrollPane");

        textAreaPanel.add(scrollableTextArea, BorderLayout.CENTER);
    }

    private void initTopPanel(JPanel topPanel) {
        JButton saveButton = new JButton(new ImageIcon("images/save-icon.jpg"));
        saveButton.setName("SaveButton");
        saveButton.setToolTipText("Save file");
        saveButton.setMargin(new Insets(0, 0, 0, 0));
        saveButton.addActionListener(this::saveButtonAction);
        topPanel.add(saveButton);

        JButton openButton = new JButton(new ImageIcon("images/open-icon.jpg"));
        openButton.setName("OpenButton");
        openButton.setToolTipText("Open file");
        openButton.setMargin(new Insets(0, 0, 0, 0));
        openButton.addActionListener(this::openButtonAction);
        topPanel.add(openButton);

        searchTextField.setName("SearchField");
        searchTextField.setPreferredSize(new Dimension(145, 32));
        searchTextField.setFont(searchTextField.getFont().deriveFont(14.f));
        topPanel.add(searchTextField);

        JButton searchButton = new JButton(new ImageIcon("images/lupe-icon.jpg"));
        searchButton.setName("StartSearchButton");
        searchButton.setToolTipText("Start search");
        searchButton.setMargin(new Insets(0, 0, 0, 0));
        searchButton.addActionListener(this::searchButtonAction);
        topPanel.add(searchButton);

        JButton previousButton = new JButton(new ImageIcon("images/back-icon.jpg"));
        previousButton.setName("PreviousMatchButton");
        previousButton.setToolTipText("Previous match");
        previousButton.setMargin(new Insets(0, 0, 0, 0));
        previousButton.addActionListener(this::previousButtonAction);
        topPanel.add(previousButton);

        JButton nextButton = new JButton(new ImageIcon("images/forward-icon.jpg"));
        nextButton.setName("NextMatchButton");
        nextButton.setToolTipText("Next match");
        nextButton.setMargin(new Insets(0, 0, 0, 0));
        nextButton.addActionListener(this::nextButtonAction);
        topPanel.add(nextButton);

        useRegexCheckBox.setFont(useRegexCheckBox.getFont().deriveFont(14.f));
        useRegexCheckBox.setText("Use regex");
        useRegexCheckBox.setName("UseRegExCheckbox");
        topPanel.add(useRegexCheckBox);
    }

    private void nextButtonAction(ActionEvent actionEvent) {
        if (searchConditionsChanged()) {
            searchButtonAction(actionEvent);
        } else {
            textSearcher.nextResult();
        }
    }

    private void previousButtonAction(ActionEvent actionEvent) {
        if (searchConditionsChanged()) {
            searchButtonAction(actionEvent);
        } else {
            textSearcher.previousResult();
        }
    }

    private boolean searchConditionsChanged() {
        return textSearcher == null
                || !textSearcher.getText().equals(textArea.getText())
                || !textSearcher.getSearchString().equals(searchTextField.getText())
                || !textSearcher.isUseRegex() == useRegexCheckBox.isSelected();
    }

    private void searchButtonAction(ActionEvent actionEvent) {
        if (searchTextField.getText().isEmpty()) {
            textArea.setCaretPosition(0);
            textArea.grabFocus();
            return;
        }
        textSearcher = new TextSearcher(textArea.getText(),
                searchTextField.getText(),
                useRegexCheckBox.isSelected(),
                textArea);
        textSearcher.execute();
    }

    void saveButtonAction(ActionEvent e) {
        fileChooser.setVisible(true);
        int returnValue = fileChooser.showSaveDialog(null);
        fileChooser.setVisible(false);

        if (returnValue != JFileChooser.APPROVE_OPTION) {
            return;
        }

        filename = fileChooser.getSelectedFile().getAbsolutePath();
        System.out.println("Save dialog: selected file - " + filename);

        try (FileWriter fileWriter = new FileWriter(filename, false)){
            fileWriter.write(textArea.getText());
            System.out.println("saved to " + filename);
        } catch (IOException ioException) {
            System.out.println("save: IOException: " + ioException);
            ioException.printStackTrace();
        }
    }

    void openButtonAction(ActionEvent e) {
        fileChooser.setVisible(true);
        int returnValue = fileChooser.showOpenDialog(null);
        fileChooser.setVisible(false);

        if (returnValue != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        filename = selectedFile.getAbsolutePath();
        System.out.println("Open dialog: selected file - " + filename);

        try {
            String data = new String(Files.readAllBytes(Path.of(filename)));
            textArea.setText(data);
            System.out.println("load: loaded from " + filename);
        } catch (NoSuchFileException noSuchFileException) {
            System.out.println("load: IOException: " + noSuchFileException);
            textArea.setText("");
        } catch (IOException ioException) {
            System.out.println("load: IOException: " + ioException);
            ioException.printStackTrace();
        }
    }

}
