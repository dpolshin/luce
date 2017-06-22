package foo.bar.luce;

import foo.bar.luce.model.Token;

public class ToLowerCaseFilter implements TokenFilter {
    @Override
    public Token filter(Token token) {
        //todo: return same token instance;
        return new Token(token.getToken().toLowerCase(), token.getStartOffset(), token.getEndOffset());
    }
}
