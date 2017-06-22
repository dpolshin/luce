package foo.bar.luce.model;


import java.util.List;

public class SearchResultItem {
    String filename;
    String term;
    List<Position> positions;

    public SearchResultItem(String filename, String term, List<Position> positions) {
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

    public List<Position> getPositions() {
        return positions;
    }

    @Override
    public String toString() {
        return "file:" + getFilename() + " hits:" + getPositions().size();
    }
}
