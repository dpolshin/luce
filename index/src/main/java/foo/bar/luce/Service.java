package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.SearchResultItem;
import foo.bar.luce.monitoring.ChangesMonitor;
import foo.bar.luce.persistence.Persister;

import java.util.Collection;
import java.util.List;

/**
 * Services facade.
 */
public class Service {

    private static final Service INSTANCE = new Service();

    private IndexRegistry indexRegistry;
    private FileRegistry fileRegistry;
    private Indexer indexer;
    private Finder finder;
    private ChangesMonitor changesMonitor;


    public static Service getInstance() {
        return INSTANCE;
    }


    public Service() throws RuntimeException {
        Persister persister = new Persister();
        fileRegistry = new FileRegistry(persister);
        indexRegistry = new IndexRegistry(persister);
        indexer = new Indexer(indexRegistry, fileRegistry);
        finder = new Finder(indexRegistry, fileRegistry);
        changesMonitor = new ChangesMonitor(fileRegistry, indexer);
    }


    public List<SearchResultItem> search(String term) {
        return finder.find(term);
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
        //todo: not thread safe
        indexer.index(fileDescriptor);
        changesMonitor.register(fileDescriptor.getFile().toPath());
        return fileRegistry.add(fileDescriptor);
    }


    public boolean removeFileFromIndex(FileDescriptor fileDescriptor) {
        //todo: not thread safe
        return fileRegistry.remove(fileDescriptor) &&
                indexRegistry.remove(fileDescriptor);
    }

}
