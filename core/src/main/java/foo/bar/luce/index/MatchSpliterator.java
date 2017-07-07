package foo.bar.luce.index;

import foo.bar.luce.model.Token;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.regex.Matcher;

/**
 * Provide Stream interface support around regex Matcher.
 */
public class MatchSpliterator extends Spliterators.AbstractSpliterator<Token> {
    private final Matcher matcher;

    public MatchSpliterator(Matcher m) {
        super(Long.MAX_VALUE, ORDERED | NONNULL);
        matcher = m;
    }

    public boolean tryAdvance(Consumer<? super Token> action) {
        if (!matcher.find()) {
            return false;
        }
        action.accept(new Token(matcher.group(), matcher.start()));
        return true;
    }
}
