package foo.bar.luce.index;

import foo.bar.luce.model.Token;

import java.util.Spliterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Simple tokenizer.
 * Emits tokens consisting of continuous sequence of unicode letters.
 *
 * @see Character#isLetter(char)
 */
public class WordTokenizer implements Tokenizer {
    private static final String PATTERN = "\\p{L}+"; //unicode 'word' pattern
    Spliterator<Token> spliterator;

    public WordTokenizer(CharSequence input) {
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(input);
        spliterator = new MatchSpliterator(matcher);
    }

    @Override
    public Stream<Token> stream() {
        return StreamSupport.stream(spliterator, false);
    }
}
