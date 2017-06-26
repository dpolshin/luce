package foo.bar.luce.index;

import foo.bar.luce.model.Token;

import java.util.Optional;

/**
 * Apply an operation on a Token.
 */
public class ToLowerCaseFilter implements TokenFilter {

    @Override
    public Optional<Token> apply(Token token) {
        token.setToken(token.getToken().toLowerCase());
        return Optional.of(token);
    }
}
