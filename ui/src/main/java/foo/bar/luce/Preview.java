package foo.bar.luce;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import foo.bar.luce.model.SearchResultItem;
import foo.bar.luce.model.Token;
import foo.bar.luce.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Show search result file with text highlights.
 */
public class Preview extends JDialog {
    private static final Logger LOG = LoggerFactory.getLogger(Preview.class);

    private JPanel contentPane;
    private JButton close;
    private JTextArea textArea;
    private JButton prev;
    private JButton next;
    private Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
    private String filename;
    private SearchResultItem searchResult;
    private int position = 0;


    public Preview(SearchResultItem searchResult) {
        setIconImages(WindowUtil.getIcons());
        this.searchResult = searchResult;
        filename = searchResult.getFilename();
        setTitle("Preview file: " + filename);
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(close);

        close.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        try {
            File file = new File(filename);
            long size = Files.size(file.toPath());

            if (size > Constants.PREVIEW_MAX_FILE_SIZE) {
                LOG.info("file {} is too big for preview", file.getName());

                StringBuilder b = new StringBuilder("File is too big for preview, but here is list of positions matching query within file: \n\n");
                for (Token<String> t : searchResult.getPositions()) {
                    b.append(t.getPosition()).append("\n");
                    if (t.getPosition() > Constants.MAX_SEARCH_RESULT_SIZE) {
                        break;
                    }
                }
                textArea.setText(b.toString());

            } else {
                CharSequence charSequence = FileUtil.fromFile(file);
                textArea.setText(charSequence.toString());
                for (Token<String> t : searchResult.getPositions()) {
                    int position = t.getPosition();
                    String token = t.getToken();
                    highlight(textArea, position, position + token.length());
                }
                //set position to first match
                //or use custom create to avoid textArea content change
                moveToPosition(0);

                next.addActionListener(e -> moveToPosition(position + 1));
                prev.addActionListener(e -> moveToPosition(position - 1));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Creates highlights around all occurrences of pattern in textComp
    private void highlight(JTextComponent textComp, int start, int end) {
        try {
            Highlighter highlighter = textComp.getHighlighter();
            highlighter.addHighlight(start, end, highlightPainter);

        } catch (BadLocationException e) {
            LOG.error("highlight position is out of scope", e);
        }
    }

    private void moveToPosition(int entry) {
        try {
            int position = getMatchPosition(entry);
            textArea.setCaretPosition(position);
        } catch (IllegalArgumentException e) {
            textArea.setCaretPosition(0);
            LOG.error("file content for {} changed and position no longer available", filename);
        }
    }

    private int getMatchPosition(int entry) {
        List<Token<String>> positions = searchResult.getPositions();
        int size = positions.size();
        int newPosition;

        if (entry <= 0) {
            newPosition = 0;
        } else if (entry < size) {
            newPosition = entry;
        } else {
            newPosition = size - 1;
        }

        position = newPosition;
        return positions.get(newPosition).getPosition();
    }

    private void onCancel() {
        dispose();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(5, 5, 5, 5), -1, -1));
        contentPane.setPreferredSize(new Dimension(800, 600));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        prev = new JButton();
        prev.setText("<");
        prev.setToolTipText("Previous");
        panel2.add(prev, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        close = new JButton();
        close.setText("Close");
        panel2.add(close, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        next = new JButton();
        next.setText(">");
        next.setToolTipText("Next");
        panel2.add(next, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel3.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        textArea = new JTextArea();
        textArea.setColumns(50);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setRows(10);
        textArea.setWrapStyleWord(true);
        scrollPane1.setViewportView(textArea);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
