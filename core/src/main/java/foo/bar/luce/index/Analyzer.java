package foo.bar.luce.index;

import foo.bar.luce.model.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Stream;


/**
 * Apply token filter chain to token stream.
 */
public class Analyzer {
    private static final Logger LOG = LoggerFactory.getLogger(Analyzer.class);

    private TokenFilter filterChain;

    public Analyzer() {
        this.filterChain = new ToLowerCaseFilter();//.then(new StopWordsFilter());
    }

    public Stream<Token> analyze(Token token) {
        return Stream.of(token)
                .map(t -> filterChain.apply(t))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
