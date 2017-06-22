package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.SearchResultItem;

import java.io.File;
import java.util.Collection;
import java.util.Set;

public class Service {

    private static final Service INSTANCE = new Service();

    private MemoryIndexRepository indexRepository = new MemoryIndexRepository();
    private FileRegistry fileRegistry = new FileRegistry();
    private Indexer indexer = new Indexer(indexRepository);


    public static Service getInstance() {
        return INSTANCE;
    }


    public Set<SearchResultItem> search(String term) {
        return indexRepository.lookup(term);
    }


    /**
     * List all files that were added to index.
     *
     * @return indexed files
     */
    public Collection<String> getIndexedFiles() {
        return fileRegistry.getIndexedFiles();
    }


    public boolean addFileToIndex(FileDescriptor fileDescriptor) {
        File file = fileDescriptor.getFile();
        indexer.index(file);
        return fileRegistry.add(fileDescriptor);
    }


    public boolean removeFileFromIndex(FileDescriptor fileDescriptor) {
        return fileRegistry.drop(fileDescriptor) &&
        indexRepository.drop(fileDescriptor);
    }


    public boolean updateIndex(FileDescriptor fileDescriptor) {
        return fileRegistry.update(fileDescriptor);
    }
}
