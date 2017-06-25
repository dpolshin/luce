package foo.bar.luce.ui;

import foo.bar.luce.model.Position;
import foo.bar.luce.model.SearchResultItem;
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

/**
 * Show search result file with text highlights.
 */
public class Preview extends JDialog {
    private static final Logger LOG = LoggerFactory.getLogger(Preview.class);

    private JPanel contentPane;
    private JButton close;
    private JTextArea textArea;
    private Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);

    public Preview(SearchResultItem searchResult) {
        String filename = searchResult.getFilename();
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
            //todo: limit preview size
            CharSequence charSequence = FileUtil.fromFile(new File(filename));
            textArea.setText(charSequence.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Position p : searchResult.getPositions()) {
            highlight(textArea, p.getStart(), p.getEnd());
        }

        //set position to start
        //or use custom create to avoid textArea content change
        try {
            textArea.setCaretPosition(searchResult.getPositions().get(0).getStart());
        } catch (IllegalArgumentException e) {
            textArea.setCaretPosition(0);
            LOG.error("file content for {} changed and position no longer available", filename);

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

    private void onCancel() {
        dispose();
    }

}
