package foo.bar.luce;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WindowUtil {
    private static final int STATUS_LEN = 50;

    /**
     * Load global application icons.
     *
     * @return icon set
     */
    public static List<Image> getIcons() {
        //noinspection ConstantConditions
        return Stream.of("icon/i16.png", "icon/i32.png", "icon/i64.png", "icon/i128.png")
                .map(s -> new ImageIcon(Application.class.getClassLoader().getResource(s)).getImage())
                .collect(Collectors.toList());
    }

    /**
     * Truncate if necessary string for status bar text.
     * Text is cut from the middle of a string.
     *
     * @param s source string
     * @return result string
     */
    public static String trunkateStatus(String s) {
        int length = s.length();
        if (length <= STATUS_LEN) {
            return s;
        } else {
            String overcastPlaceholder = " ... ";
            int overcast = length - 45;
            String left = s.substring(0, (length - overcast) / 2);
            String right = s.substring((length + overcast) / 2, length);
            return left + overcastPlaceholder + right;
        }
    }

}