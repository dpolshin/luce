package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.IndexEntry;
import foo.bar.luce.model.SearchResultItem;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Simple in-memory inverted index with segmentation by single file.
 */
public class MemoryIndexRepository {

    private Map<FileDescriptor, Map<String, IndexEntry>> indexes = new TreeMap<>();

    public void addOrUpdate(FileDescriptor fileDescriptor, Map<String, IndexEntry> index) {
        indexes.put(fileDescriptor, index);
    }

    public boolean drop(FileDescriptor fileDescriptor) {
        return indexes.remove(fileDescriptor) != null;
    }

    public Set<SearchResultItem> lookup(String term) {
        return indexes.entrySet().parallelStream()
                .filter(entry -> entry.getValue().containsKey(term))
                .map(entry -> new SearchResultItem(entry.getKey().getLocation(), term, entry.getValue().get(term).getTokens()))
                .sequential().collect(Collectors.toSet());
    }


}
