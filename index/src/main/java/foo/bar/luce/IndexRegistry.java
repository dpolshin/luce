package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.SearchResultItem;
import foo.bar.luce.persistence.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Simple inverted index with segmentation by single file.
 */
public class IndexRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(IndexRegistry.class);

    private Map<FileDescriptor, IndexSegment> indexCache = new TreeMap<>();
    private Persister persister;
    private FileRegistry fileRegistry;

    public IndexRegistry(Persister persister, FileRegistry fileRegistry) {
        this.persister = persister;
        this.fileRegistry = fileRegistry;
    }

    public void addOrUpdate(FileDescriptor fileDescriptor, IndexSegment segment) {
        persister.save(segment);
        indexCache.put(fileDescriptor, segment);
    }

    public boolean drop(FileDescriptor fileDescriptor) {
        //persister.load(fileDescriptor.getIndexSegmentId());
        return indexCache.remove(fileDescriptor) != null;
    }

    public List<SearchResultItem> lookup(String term) {
        //fast cache search
        List<SearchResultItem> searchResult = indexCache.entrySet().parallelStream()
                .filter(entry -> entry.getValue().getSegment().containsKey(term))
                .map(entry -> new SearchResultItem(entry.getKey().getLocation(), term, entry.getValue().getSegment().get(term)))
                .sequential().collect(Collectors.toList());


        //load and search persisted files
        Set<FileDescriptor> indexedFiles = fileRegistry.getIndexedFilDescriptors();
        Set<FileDescriptor> cachedFiles = indexCache.keySet();

        Set<FileDescriptor> remaining = indexedFiles.stream().filter(e -> !cachedFiles.contains(e)).collect(Collectors.toSet());

        for (FileDescriptor file : remaining) {
            LOG.info("no file in cache, loading index segment for {}", file.getLocation());
            IndexSegment indexSegment = persister.load(file.getIndexSegmentId());
            if (indexSegment != null) {
                indexCache.put(file, indexSegment);
            } else {
                LOG.info("index segment file not found for {}", file.getLocation());
                continue;
            }

            if (indexSegment.getSegment().containsKey(term)) {
                searchResult.add(new SearchResultItem(file.getLocation(), term, indexSegment.getSegment().get(term)));
            }
        }
        return searchResult;
    }
}
