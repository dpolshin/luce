package foo.bar.luce.model;

import foo.bar.luce.persistence.Persistable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class representing added to index file names, mapped to disk persistent storage.
 */
public class FileSegment implements Persistable {
    private static final long serialVersionUID = 7319446910401537232L;

    private Set<FileDescriptor> indexedFiles = ConcurrentHashMap.newKeySet();
    private Set<FileDescriptor> watchRoots = ConcurrentHashMap.newKeySet();

    @Override
    public String getId() {
        return FileSegment.class.getSimpleName();
    }

    public Set<FileDescriptor> getIndexedFiles() {
        return indexedFiles;
    }

    public Set<FileDescriptor> getWatchRoots() {
        return watchRoots;
    }
}
