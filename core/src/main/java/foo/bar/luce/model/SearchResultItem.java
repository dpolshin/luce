package foo.bar.luce.model;


import java.util.List;

/**
 * Represents a file that matched to search term and positions of a term within file.
 */
public class SearchResultItem {
    private String filename;
    private String term;
    private List<Integer> positions;

    public SearchResultItem(String filename, String term, List<Integer> positions) {
        this.filename = filename;
        this.term = term;
        this.positions = positions;
    }

    public String getFilename() {
        return filename;
    }

    public String getTerm() {
        return term;
    }

    public List<Integer> getPositions() {
        return positions;
    }

    @Override
    public String toString() {
        return "file:" + getFilename() + " hits:" + getPositions().size();
    }
}
