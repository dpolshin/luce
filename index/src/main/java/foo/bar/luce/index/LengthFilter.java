package foo.bar.luce.index;

import foo.bar.luce.model.Token;

import java.util.Optional;

public class LengthFilter implements TokenFilter {
    private static final int min = 3;
    private static final int max = 20;

    @Override
    public Optional<Token> apply(Token token) {
        int length = token.getToken().length();
        if (length >= min && length <= max) {
            return Optional.of(token);
        } else {
            return Optional.empty();
        }
    }
}
