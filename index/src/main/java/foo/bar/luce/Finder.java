package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.IndexSegment;
import foo.bar.luce.model.SearchResultItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Finder {
    private static final Logger LOG = LoggerFactory.getLogger(Finder.class);

    private IndexRegistry indexRegistry;
    private FileRegistry fileRegistry;


    public Finder(IndexRegistry indexRegistry, FileRegistry fileRegistry) {
        this.indexRegistry = indexRegistry;
        this.fileRegistry = fileRegistry;
    }


    public List<SearchResultItem> find(String term) {
        //fast cache search
        Map<FileDescriptor, IndexSegment> indexCache = indexRegistry.getIndexCache();
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
            IndexSegment indexSegment = indexRegistry.getIndexSegment(file);
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
