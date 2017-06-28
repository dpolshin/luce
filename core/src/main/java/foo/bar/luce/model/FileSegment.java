package foo.bar.luce.model;

import foo.bar.luce.persistence.Persistable;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class representing added to index file names, mapped to disk persistent storage.
 */
public class FileSegment implements Persistable {
    private Set<FileDescriptor> indexedFiles = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public String getId() {
        return FileSegment.class.getSimpleName();
    }

    public Set<FileDescriptor> getIndexedFiles() {
        return indexedFiles;
    }
}
