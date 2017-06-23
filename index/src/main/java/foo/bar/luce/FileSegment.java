package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.persistence.Persistable;

import java.util.HashSet;
import java.util.Set;

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
