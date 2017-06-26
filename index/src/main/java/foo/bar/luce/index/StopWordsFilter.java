package foo.bar.luce.index;

import foo.bar.luce.model.Token;

import java.util.*;

public class StopWordsFilter implements TokenFilter {
    private static final Set<String> stopWords;

    static {
        final List<String> stopWordsList = Arrays.asList(
                "a", "an", "and", "are", "as", "at", "be", "but", "by",
                "for", "if", "in", "into", "is", "it",
                "no", "not", "of", "on", "or", "such",
                "that", "the", "their", "then", "there", "these",
                "they", "this", "to", "was", "will", "with"
        );
        stopWords = new HashSet<>();
        stopWords.addAll(stopWordsList);
    }

    @Override
    public Optional<Token> apply(Token token) {
        if (!stopWords.contains(token.getToken())) {
            return Optional.of(token);
        } else {
            return Optional.empty();
        }
    }
}
