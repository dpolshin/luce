package foo.bar.luce.index;

import foo.bar.luce.model.Token;

import java.util.Optional;

/**
 * Apply an operation on a Token.
 */
public class ToLowerCaseStringFilter implements TokenFilter<String> {

    @Override
    public Optional<Token<String>> apply(Token<String> token) {
        token.setToken(token.getToken().toLowerCase());
        return Optional.of(token);
    }
}
