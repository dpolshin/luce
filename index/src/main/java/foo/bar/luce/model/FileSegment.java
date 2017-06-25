package foo.bar.luce.model;

import foo.bar.luce.persistence.Persistable;

import java.util.HashSet;
import java.util.Set;

/**
 * Class representing added to index file names, mapped to disk persistent storage.
 */
public class FileSegment implements Persistable {
    private Set<FileDescriptor> indexedFiles = new HashSet<>();

    @Override
    public String getId() {
        return FileSegment.class.getSimpleName();
    }

    public Set<FileDescriptor> getIndexedFiles() {
        return indexedFiles;
    }
}
