package foo.bar.luce.model;


import java.util.List;

/**
 * Represents a file that matched to search term and positions of a term within file.
 */
public class SearchResultItem {
    private String filename;
    private List<Token<String>> positions;

    public SearchResultItem(String filename, List<Token<String>> positions) {
        this.filename = filename;
        this.positions = positions;
    }

    public String getFilename() {
        return filename;
    }

    public List<Token<String>> getPositions() {
        return positions;
    }

    @Override
    public String toString() {
        return "file:" + getFilename() + " hits:" + getPositions().size();
    }
}
