package foo.bar.luce.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Class representing all positions of a term within a file.
 */
public class IndexEntry {
    private String term;
    private List<Position> tokens;

    public IndexEntry(Token token) {
        this.term = token.getToken();
        this.tokens = new LinkedList<>();
        tokens.add(token.getPosition());
    }

    public List<Position> getTokens() {
        return tokens;
    }

    public String getTerm() {
        return term;
    }
}
