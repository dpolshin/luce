package foo.bar.luce.index;

import foo.bar.luce.model.Token;

import java.util.*;

/**
 * Filter three and more letters stop words.
 * Shorter words will be filtered out by {@link foo.bar.luce.index.LengthFilter}.
 */
public class StopWordsFilter implements TokenFilter {
    private static final Set<String> stopWords;

    static {
        List<String> stopWordsListEn = Arrays.asList(
                "and", "are", "but", "for", "into", "not", "such", "that", "the", "their",
                "then", "there", "these", "they", "this", "was", "will", "with"
        );
        List<String> stopWordsListRu = Arrays.asList("без",
                "вон", "вот", "все", "еще", "даже", "для", "если", "еще", "ещё", "зато", "зачем", "или", "как", "какая",
                "какой", "кем", "кого", "ком", "кому", "конечно", "которая", "которого", "которой", "которые", "который",
                "которых", "кроме", "кто", "куда", "лишь", "над", "так", "такая", "также", "таки", "такие", "такое", "такой");
        stopWords = new HashSet<>();
        stopWords.addAll(stopWordsListEn);
        stopWords.addAll(stopWordsListRu);
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
