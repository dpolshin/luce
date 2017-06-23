package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.SearchResultItem;
import foo.bar.luce.persistence.Persister;

import java.io.File;
import java.util.Collection;
import java.util.List;

public class Service {

    private static final Service INSTANCE = new Service();

    private Persister persister;
    private IndexRegistry indexRepository;
    private FileRegistry fileRegistry;
    private Indexer indexer;


    public static Service getInstance() {
        return INSTANCE;
    }


    public Service() throws RuntimeException {
        persister = new Persister();
        fileRegistry = new FileRegistry(persister);

        indexRepository = new IndexRegistry(persister, fileRegistry);
        indexer = new Indexer(indexRepository);
    }


    public List<SearchResultItem> search(String term) {
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
        persister.delete(fileDescriptor.getIndexSegmentId());
        return fileRegistry.drop(fileDescriptor) &&
                indexRepository.drop(fileDescriptor);
    }


    public boolean updateIndex(FileDescriptor fileDescriptor) {
        return fileRegistry.update(fileDescriptor);
    }
}
