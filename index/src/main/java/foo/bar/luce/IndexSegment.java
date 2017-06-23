package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.Position;
import foo.bar.luce.persistence.Persistable;

import java.util.List;
import java.util.Map;

public class IndexSegment implements Persistable {
    private Map<String, List<Position>> segment;
    private String id;

    public IndexSegment(FileDescriptor fileDescriptor, Map<String, List<Position>> segment) {
        this.segment = segment;
        this.id = fileDescriptor.getIndexSegmentId();
    }

    @Override
    public String getId() {
        return id;
    }

    public Map<String, List<Position>> getSegment() {
        return segment;
    }
}
