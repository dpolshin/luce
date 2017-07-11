package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.FileSegment;
import foo.bar.luce.persistence.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Service that manages list of added files.
 */
public class FileRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(FileRegistry.class);

    private FileSegment fileSegment;
    private Persister persister;

    private AtomicBoolean dirty = new AtomicBoolean(false);


    public FileRegistry(Persister persister) {
        this.persister = persister;

        //FileRegistry id for persister is always it's classname
        FileSegment segment = persister.load(FileSegment.class.getSimpleName());
        if (segment != null) {
            fileSegment = segment;
        } else {
            fileSegment = new FileSegment();
        }

        new Timer().scheduleAtFixedRate(new TimerTask() {
            public void run() {
                persistDirty();
            }
        }, 0, 60000); //minute
        Runtime.getRuntime().addShutdownHook(new Thread(this::persist));
    }

    public boolean remove(FileDescriptor fileDescriptor) {
        LOG.info("dropping file from index: {}", fileDescriptor.getLocation());
        boolean remove = fileSegment.getIndexedFiles().remove(fileDescriptor);
        fileDescriptor.getIndexSegmentIds().forEach(chunk -> {
            persister.remove(chunk);
            dirty.set(true);
        });
        return remove;
    }

    public void addOrUpdate(FileDescriptor fileDescriptor) {
        LOG.info("updating file from index: {}", fileDescriptor.getLocation());
        fileSegment.getIndexedFiles().remove(fileDescriptor);
        fileSegment.getIndexedFiles().add(fileDescriptor);
        dirty.set(true);
    }

    public boolean isIndexed(FileDescriptor fd) {
        return fileSegment.getIndexedFiles().contains(fd);
    }

    public Set<String> getIndexedFiles() {
        return fileSegment.getIndexedFiles().stream().map(FileDescriptor::getLocation).collect(Collectors.toSet());
    }

    public Set<FileDescriptor> getIndexedFilDescriptors() {
        return fileSegment.getIndexedFiles();
    }

    public void persist() {
        persister.saveDefault(fileSegment);
    }

    private void persistDirty() {
        if (dirty.get()) {
            LOG.debug("persisting files registry");
            persist();
            dirty.set(false);
        }
    }
}
