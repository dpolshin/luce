package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FileRegistry {

    private Set<FileDescriptor> indexedFiles = new HashSet<>();


    public boolean add(FileDescriptor fileDescriptor) {
        System.out.println("adding file to index: " + fileDescriptor.getLocation());
        return indexedFiles.add(fileDescriptor);
    }

    public boolean drop(FileDescriptor fileDescriptor) {
        System.out.println("dropping file from index: " + fileDescriptor.getLocation());
        return indexedFiles.remove(fileDescriptor);
    }

    public boolean update(FileDescriptor fileDescriptor) {
        System.out.println("updating file from index: " + fileDescriptor.getLocation());
        return indexedFiles.remove(fileDescriptor) &&
                indexedFiles.add(fileDescriptor);
    }

    public Set<String> getIndexedFiles() {
        return indexedFiles.stream().map(FileDescriptor::getLocation).collect(Collectors.toSet());
    }
}
