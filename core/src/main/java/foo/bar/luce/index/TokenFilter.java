package foo.bar.luce.index;

import foo.bar.luce.model.Token;

import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface TokenFilter<T> extends Function<Token<T>, Optional<Token<T>>> {

    default TokenFilter<T> then(TokenFilter<T> after) {
        return (Token<T> t) -> apply(t).flatMap(after);
    }
}
