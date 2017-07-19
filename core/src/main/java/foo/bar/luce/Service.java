package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.IndexingResult;
import foo.bar.luce.model.SearchResultItem;
import foo.bar.luce.monitoring.ChangesMonitor;
import foo.bar.luce.persistence.Persister;
import foo.bar.luce.util.UnsupportedContentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Observer;
import java.util.function.Function;
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


    public void add(FileDescriptor fileDescriptor, Function<IndexingResult, Void> publisher) {
        if (fileDescriptor.getFile().isDirectory()) {
            addDirectory(fileDescriptor, publisher);
        } else {
            publisher.apply(addFile(fileDescriptor));
        }
    }

    public void subscribeToFileListChanges(Observer o) {
        fileRegistry.addObserver(o);
    }

    private void addDirectory(FileDescriptor fileDescriptor, Function<IndexingResult, Void> publisher) {
        fileRegistry.getWatchRootFilDescriptors().add(fileDescriptor);

        try {
            Files.walkFileTree(fileDescriptor.getFile().toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    if (!path.toFile().isDirectory()) {
                        addFile(new FileDescriptor(path.toFile()));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOG.error("Adding file to index failed", e);
        }
    }


    private IndexingResult addFile(FileDescriptor fileDescriptor) {
        String location = fileDescriptor.getLocation();
        IndexingResult.Code code;

        if (fileRegistry.isIndexed(fileDescriptor)) {
            code = duplicate;
        } else {
            try {
                indexer.index(fileDescriptor);
                code = ok;
                fileRegistry.getWatchRootFilDescriptors().add(fileDescriptor);
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
