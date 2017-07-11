package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.IndexSegment;
import foo.bar.luce.persistence.Persister;
import foo.bar.luce.util.CacheMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Simple inverted index with segmentation by single file.
 */
public class IndexRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(IndexRegistry.class);

    private Map<FileDescriptor, IndexSegment> indexCache = new CacheMap<>();
    private Persister persister;

    public IndexRegistry(Persister persister) {
        this.persister = persister;
    }

    public void addOrUpdate(FileDescriptor fileDescriptor, IndexSegment segment) {
        persister.save(segment);
       // indexCache.put(fileDescriptor, segment);
    }

    public boolean remove(FileDescriptor fileDescriptor) {
        if (indexCache.remove(fileDescriptor) != null) {
            LOG.info("removing cached index segment from memory {}", fileDescriptor.getLocation());
        }
        return true;
    }

    Map<FileDescriptor, IndexSegment> getIndexCache() {
        return indexCache;
    }

    public Stream<IndexSegment> getIndexSegments(FileDescriptor fileDescriptor) {
//        IndexSegment segment = indexCache.get(fileDescriptor);
//        if (segment == null) {
//            segment =
       return fileDescriptor.getIndexSegmentIds().stream().map(id -> persister.load(id, IndexSegment.class));
//        }
//        return segment;
    }

//    public void cacheSegment(FileDescriptor fileDescriptor, IndexSegment segment) {
//        indexCache.put(fileDescriptor, segment);
//    }

}
