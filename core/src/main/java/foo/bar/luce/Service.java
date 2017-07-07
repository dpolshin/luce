package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.IndexingResult;
import foo.bar.luce.model.SearchResultItem;
import foo.bar.luce.monitoring.ChangesMonitor;
import foo.bar.luce.persistence.Persister;
import foo.bar.luce.util.UnsupportedContentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.stream.Stream;

import static foo.bar.luce.model.IndexingResult.Code.*;

/**
 * Services facade.
 */
public class Service {
    private static final Logger LOG = LoggerFactory.getLogger(Service.class);
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


    public Stream<SearchResultItem> search(String term, Finder.Mode mode) {
        return finder.find(term, mode);
    }


    /**
     * List all files that were added to index.
     *
     * @return indexed files
     */
    public Collection<String> getIndexedFiles() {
        return fileRegistry.getIndexedFiles();
    }


    public IndexingResult addFileToIndex(FileDescriptor fileDescriptor) {
        String location = fileDescriptor.getLocation();
        IndexingResult.Code code;

        LOG.debug("Adding file {} to index", location);
        if (fileRegistry.getIndexedFiles().contains(location)) {
            code = duplicate;
        } else {
            try {
                indexer.index(fileDescriptor);
                changesMonitor.register(fileDescriptor.getFile().toPath());
                code = ok;
            } catch (UnsupportedContentException e) {
                LOG.info("Adding file {} failed. Corrupt file or unsupported encoding", location);
                code = unsupported;
            } catch (Exception e) {
                LOG.info("Adding file failed", e);
                code = fail;
            }
        }
        return new IndexingResult(code, location);
    }


    public boolean removeFileFromIndex(FileDescriptor fileDescriptor) {
        return fileRegistry.remove(fileDescriptor)
                && indexRegistry.remove(fileDescriptor);
    }

}
