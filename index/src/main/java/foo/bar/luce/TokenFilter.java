package foo.bar.luce;

import foo.bar.luce.model.Token;

@FunctionalInterface
public interface TokenFilter {
    Token filter(Token token);
}
