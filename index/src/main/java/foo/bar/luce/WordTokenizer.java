package foo.bar.luce;

import foo.bar.luce.model.Token;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple tokenizer.
 * Emits tokens consisting of continuous sequence of unicode letters.
 *
 * @see Character#isLetter(char)
 */
public class WordTokenizer {
    private static final String PATTERN = "\\p{L}+"; //unicode 'word' pattern
    private final Matcher matcher;

    public WordTokenizer(CharSequence input) {
        Pattern pattern = Pattern.compile(PATTERN);
        matcher = pattern.matcher(input);
    }

    public Token next() {
        if (matcher.find()) {
            return new Token(matcher.group(), matcher.start(), matcher.end());
        } else {
            return null;
        }
    }
}
