package foo.bar.luce;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.Position;
import foo.bar.luce.model.SearchResultItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * Application GUI entry point.
 */
public class Application extends JFrame {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private JTabbedPane tabs;
    private JTextField searchTerm;
    private JButton searchButton;
    private JButton addButton;
    private JButton removeButton;
    private JPanel root;
    private JList searchResult;
    private JList fileList;
    private JScrollPane fileScrollPane;
    private JTextArea status;
    private JCheckBox exactCheckBox;


    private DefaultListModel<String> fileListModel;
    private DefaultListModel<SearchResultItem> searchListModel;


    /**
     * Main service instance facade.
     */
    private Service service;


    public static void main(String[] args) {
        new Application();
    }


    public Application() {
        super("LuceBrother (tm) Simple text indexing and search tool");
        $$$setupUI$$$();
        LOG.debug("loading application");

        service = Service.getInstance();

        setContentPane(root);
        setSize(800, 600);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        status.setText("Application loaded");


        //search
        searchTerm.registerKeyboardAction(e -> search(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        searchButton.addActionListener(e -> {
            search();
        });


        //add files
        addButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();

            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files", "txt");
            chooser.setFileFilter(filter);
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int returnVal = chooser.showOpenDialog(root);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();


                addFile(file);

            }
        });


        //remove files
        //todo: disable button if list is empty;
        removeButton.addActionListener(e -> {
            int index = fileList.getSelectedIndex();

            String filename = fileListModel.get(index);

            boolean removed = service.removeFileFromIndex(new FileDescriptor(filename));

            if (removed) {
                fileListModel.remove(index);
                int size = fileListModel.getSize();

                if (size == 0) {
                    removeButton.setEnabled(false);

                } else {
                    if (index == fileListModel.getSize()) {
                        index--;
                    }

                    fileList.setSelectedIndex(index);
                    fileList.ensureIndexIsVisible(index);
                }
                status.setText("File " + filename + " removed from index");
            }
        });


        //show text preview
        searchResult.addListSelectionListener(e -> {
            SearchResultItem selectedValue = (SearchResultItem) searchResult.getSelectedValue();
            if (selectedValue != null) {
                SwingUtilities.invokeLater(() -> {
                    Preview preview = new Preview(selectedValue);
                    preview.pack();
                    preview.setVisible(true);
                });
            }
        });
    }

    private void addFile(File file) {
        if (!file.isDirectory()) {
            addSingleFile(file);
        } else {
            try {
                Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                        FileFilter txtExtensionFilter = file1 -> file1.getAbsolutePath().toLowerCase().endsWith(".txt");

                        if (txtExtensionFilter.accept(file.toFile())) {
                            addSingleFile(file.toFile());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                LOG.error("Adding file to index failed", e);
            }
        }
    }

    private void addSingleFile(File file) {
        if (service.addFileToIndex(new FileDescriptor(file))) {
            fileListModel.addElement(file.getAbsolutePath());

            status.setText("File " + file.getName() + " added to index");
            LOG.info("file {} added to index", file.getAbsolutePath());

            //todo: move button toggle to separate listener;
            removeButton.setEnabled(true);
        } else {
            LOG.info("file {} already indexed", file.getAbsolutePath());
        }
    }

    private void search() {
        String term = searchTerm.getText();
        LOG.info("search for term: {}", term);

        List<SearchResultItem> searchResults = service.search(term, selectedMode());
        status.setText("Found " + searchResults.size() + " files");
        LOG.info("found {} results for term {}", searchResults.size(), term);
        searchListModel.removeAllElements();

        for (SearchResultItem item : searchResults) {
            searchListModel.addElement(item);

            StringBuilder b = new StringBuilder();
            for (Position p : item.getPositions()) {
                b.append(p.getStart()).append("-").append(p.getEnd()).append(",");
            }
            LOG.debug("file: {} match positions: {}", item.getFilename(), b.toString());
        }
    }


    private void createUIComponents() {
        createFileListModel();
        createSearchResultModel();
    }


    //create list of existing files under index
    private void createFileListModel() {
        fileListModel = new DefaultListModel<>();

        for (String file : Service.getInstance().getIndexedFiles()) {
            fileListModel.addElement(file);
        }
        //noinspection unchecked
        fileList = new JList(fileListModel);
    }

    //create empty search result
    private void createSearchResultModel() {
        searchListModel = new DefaultListModel<>();
        //noinspection unchecked
        searchResult = new JList(searchListModel);
    }

    private Finder.Mode selectedMode() {
        if (exactCheckBox.isSelected()) {
            return Finder.Mode.Exact;
        } else {
            return Finder.Mode.All;
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        root = new JPanel();
        root.setLayout(new GridLayoutManager(2, 1, new Insets(5, 5, 5, 5), -1, -1));
        root.setPreferredSize(new Dimension(800, 600));
        tabs = new JTabbedPane();
        root.add(tabs, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 357), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        tabs.addTab("Search", panel1);
        searchTerm = new JTextField();
        panel1.add(searchTerm, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 1, false));
        searchButton = new JButton();
        searchButton.setText("Search");
        panel1.add(searchButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        searchResult.setEnabled(true);
        scrollPane1.setViewportView(searchResult);
        exactCheckBox = new JCheckBox();
        exactCheckBox.setText("exact");
        panel1.add(exactCheckBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        tabs.addTab("Index", panel2);
        addButton = new JButton();
        addButton.setText("Add");
        panel2.add(addButton, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        removeButton = new JButton();
        removeButton.setText("Remove");
        panel2.add(removeButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fileScrollPane = new JScrollPane();
        panel2.add(fileScrollPane, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        fileList.setSelectionMode(0);
        fileScrollPane.setViewportView(fileList);
        status = new JTextArea();
        status.setBackground(new Color(-1184275));
        status.setDisabledTextColor(new Color(-16777216));
        status.setEditable(false);
        status.setEnabled(false);
        status.setFocusCycleRoot(false);
        status.setFocusable(false);
        status.setMargin(new Insets(5, 5, 5, 5));
        root.add(status, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 17), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return root;
    }
}
