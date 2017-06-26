package foo.bar.luce.index;

import foo.bar.luce.model.Token;

import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface TokenFilter extends Function<Token, Optional<Token>> {

    default TokenFilter then(TokenFilter after) {
        return (Token t) -> apply(t).flatMap(after);
    }
}
