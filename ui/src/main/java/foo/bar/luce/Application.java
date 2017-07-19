package foo.bar.luce;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.IndexingResult;
import foo.bar.luce.model.SearchResultItem;
import foo.bar.luce.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.function.Function;

/**
 * Application GUI entry point.
 */
public class Application extends JFrame implements Observer {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private final Object searchMonitor = new Object();

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
    private JProgressBar progressBar;


    private DefaultListModel<String> fileListModel;
    private DefaultListModel<SearchResultItem> searchListModel;
    private Tasks tasks;


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

        setIconImages(WindowUtil.getIcons());

        service = Service.getInstance();

        setContentPane(root);
        setSize(800, 600);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        status.setText("Application loaded");
        tasks = new Tasks(progressBar);

        service.subscribeToFileListChanges(this);

        //search
        searchTerm.registerKeyboardAction(e -> search(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        searchButton.addActionListener(e -> search());


        //add files
        addButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();

            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int returnVal = chooser.showOpenDialog(root);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                status.setText("Adding " + file.getName());
                addFile(file);

            }
            removeButton.setEnabled(true);
        });


        //remove files
        removeButton.addActionListener(e -> {
            if (fileListModel.getSize() == 0) {
                removeButton.setEnabled(false);
                return;
            }

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

        //show background tasks
        progressBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                SwingUtilities.invokeLater(() -> {
                    showJobsDialog();
                });
            }
        });
    }

    private void addFile(File file) {
        Job<IndexingResult> job = new Job<IndexingResult>("Indexing: '" + file.getName() + "'") {
            @Override
            protected String doInBackground() throws Exception {
                Function<IndexingResult, Void> publisher = (result) -> {
                    publish(result);
                    return null;
                };

                service.add(new FileDescriptor(file), publisher);
                return null;
            }

            @Override
            protected void process(List<IndexingResult> chunks) {
                IndexingResult last = chunks.get(chunks.size() - 1);
                status.setText("File: " + last.getPath() + " " + last.getCode().label);
            }

        };
        tasks.run(job);
    }


    private void search() {
        String term = searchTerm.getText();
        LOG.info("search for term: '{}'", term);
        searchListModel.removeAllElements();

        Job<SearchResultItem> job = new Job<SearchResultItem>("Searching: '" + term + "'") {
            private int count = 0;

            @Override
            protected String doInBackground() throws Exception {
                synchronized (searchMonitor) {
                    service.search(term, selectedMode()).forEach(this::publish);
                    return null;
                }
            }

            @Override
            public void onComplete() {
                status.setText("Found " + count + " files");
            }

            @Override
            protected void process(List<SearchResultItem> chunks) {
                for (SearchResultItem item : chunks) {
                    count++;
                    searchListModel.addElement(item);
                    status.setText("Found " + count + " files");
                }
            }
        };
        tasks.run(job);
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

    private void showJobsDialog() {

        Window win = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(win, "Active Jobs", Dialog.ModalityType.APPLICATION_MODAL);
        JScrollPane jobScrollPane = new JScrollPane();
        dialog.add(jobScrollPane);

        DefaultListModel<Tasks.JobDescription> jobsModel = new DefaultListModel<>();
        tasks.getRunningTasks().forEach(jobsModel::addElement);

        JList<Tasks.JobDescription> jobs = new JList<>(jobsModel);
        jobs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        jobs.addListSelectionListener(e -> {
            Tasks.JobDescription selectedJob = jobs.getSelectedValue();

            if (selectedJob != null) {
                //0 == ok
                //1 == cancel
                int selection = JOptionPane.showConfirmDialog(null,
                        "Cancel job?", "Cancel selected job?", JOptionPane.YES_NO_OPTION);

                if (selection == 0) {
                    SwingUtilities.invokeLater(() -> {
                        tasks.cancel(selectedJob);
                        jobsModel.removeElement(selectedJob);

                    });
                }
            }
        });

        // jobScrollPane.add(jobs);
        jobScrollPane.setViewportView(jobs);
        dialog.setLocationRelativeTo(this);
        dialog.pack();
        dialog.setSize(400, 100);
        dialog.setVisible(true);
    }

    @Override
    //since the only observable model is file list, it's ok to make entire app an observer.
    //otherwise files panel should become separate class..
    public void update(Observable o, Object arg) {
        SwingUtilities.invokeLater(() -> {
            //noinspection unchecked
            Pair<String, String> event = (Pair<String, String>) arg;
            String type = event.getLeft();
            String location = event.getRight();
            if (type.equals("Remove")) {
                fileListModel.removeElement(location);
            }
            if (type.equals("Add")) {
                if (!fileListModel.contains(location)) {
                    fileListModel.addElement(location);
                }
            }
        });
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
        root.setLayout(new GridLayoutManager(2, 4, new Insets(5, 5, 5, 5), -1, -1));
        root.setPreferredSize(new Dimension(800, 600));
        tabs = new JTabbedPane();
        root.add(tabs, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 357), null, 0, false));
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
        panel2.setLayout(new GridLayoutManager(4, 5, new Insets(0, 0, 0, 0), -1, -1));
        tabs.addTab("Index", panel2);
        addButton = new JButton();
        addButton.setText("Add");
        panel2.add(addButton, new GridConstraints(2, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        removeButton = new JButton();
        removeButton.setText("Remove");
        panel2.add(removeButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fileScrollPane = new JScrollPane();
        panel2.add(fileScrollPane, new GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        fileList.setSelectionMode(0);
        fileList.setVisible(true);
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
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        root.add(progressBar, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, new Dimension(100, -1), 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return root;
    }
}
