package foo.bar.luce.model;

import foo.bar.luce.persistence.Persistable;

import java.util.List;
import java.util.Map;

/**
 * Class representing part of index (usually from single file), mapped to disk persistent storage.
 */
public class IndexSegment implements Persistable {
    private static final long serialVersionUID = -6085794508951501921L;
    private Map<Character, List<Integer>> segment;
    private String id;

    public IndexSegment() {
    }

    public IndexSegment(String id) {
        this.id = id;
    }

    public IndexSegment(String segmentId, Map<Character, List<Integer>> segment) {
        this.id = segmentId;
        this.segment = segment;
    }

    @Override
    public String getId() {
        return id;
    }

    public Map<Character, List<Integer>> getSegment() {
        return segment;
    }
}
