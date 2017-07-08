package foo.bar.luce.index;

import foo.bar.luce.model.Token;
import foo.bar.luce.util.CharUtil;

import java.util.Optional;

/**
 * Apply an operation on a Token.
 */
public class ToLowerCaseCharFilter implements TokenFilter<Character> {

    @Override
    public Optional<Token<Character>> apply(Token<Character> token) {
        token.setToken(CharUtil.toLower(token.getToken()));
        return Optional.of(token);
    }
}
