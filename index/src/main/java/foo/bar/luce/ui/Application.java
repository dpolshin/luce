package foo.bar.luce.ui;

import foo.bar.luce.Service;
import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.Position;
import foo.bar.luce.model.SearchResultItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.KeyEvent;
import java.io.File;
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
            int returnVal = chooser.showOpenDialog(root);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();

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
                javax.swing.SwingUtilities.invokeLater(() -> {
                    Preview preview = new Preview(selectedValue);
                    preview.pack();
                    preview.setVisible(true);
                });
            }
        });
    }

    private void search() {
        String term = searchTerm.getText();
        LOG.info("search for term: {}", term);

        if (term.isEmpty() || term.length() == 1) {
            LOG.debug("too short search term, skipping");
            status.setText("Search term too short");
            return;
        }

        List<SearchResultItem> searchResult = service.search(term);
        status.setText("Found " + searchResult.size() + " files");
        LOG.info("found {} results for term {}", searchResult.size(), term);
        searchListModel.removeAllElements();

        for (SearchResultItem item : searchResult) {
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
}
