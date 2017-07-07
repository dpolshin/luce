package foo.bar.luce.index;

import foo.bar.luce.model.Token;

import java.util.Spliterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Simple tokenizer.
 * Emits Stream of tokens consisting of continuous sequence of unicode letters.
 *
 * @see Character#isLetter(char)
 */
public class WordTokenizer implements Tokenizer {
    private static final String PATTERN_STRING = "\\p{L}+"; //unicode 'word' pattern
    private static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    private Spliterator<Token> spliterator;

    public WordTokenizer(CharSequence input) {
        Matcher matcher = PATTERN.matcher(input);
        spliterator = new MatchSpliterator(matcher);
    }

    @Override
    public Stream<Token> stream() {
        return StreamSupport.stream(spliterator, false);
    }
}
