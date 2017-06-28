package foo.bar.luce.model;


import java.util.Map;

/**
 * Represents a file that matched to multiple search terms and positions of matched terms within file.
 * Position is reduced to just one int, representing token start.
 */
public class MultiSearchResultItem {
    private String filename;
    private Map<Integer, String> terms;

    public MultiSearchResultItem(String filename, Map<Integer, String> terms) {
        this.filename = filename;
        this.terms = terms;
    }

    public String getFilename() {
        return filename;
    }

    public Map<Integer, String> getTerms() {
        return terms;
    }


    @Override
    public String toString() {
        return "file:" + getFilename() + " hits:" + terms.size();
    }


}