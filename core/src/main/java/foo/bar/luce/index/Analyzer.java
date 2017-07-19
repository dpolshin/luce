package foo.bar.luce.index;

import foo.bar.luce.model.Token;

import java.util.Optional;
import java.util.stream.Stream;


/**
 * Apply token filter chain to token stream.
 */
public class Analyzer<T> {

    private TokenFilter<T> filterChain;

    @SafeVarargs
    public Analyzer(TokenFilter<T>... filters) {
        for (TokenFilter<T> f : filters) {
            if (filterChain == null) {
                filterChain = f;
            } else {
                filterChain = filterChain.then(f);
            }
        }
    }

    public Stream<Token<T>> analyze(Token<T> token) {
        return Stream.of(token)
                .map(t -> filterChain.apply(t))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
