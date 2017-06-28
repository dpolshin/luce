package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.FileSegment;
import foo.bar.luce.persistence.Persister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service that manages list of added files.
 */
public class FileRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(FileRegistry.class);

    private FileSegment fileSegment;
    private Persister persister;

    public FileRegistry(Persister persister) {
        this.persister = persister;

        //FileRegistry id for persister is always it's classname
        FileSegment segment = persister.load(FileSegment.class.getSimpleName());
        if (segment != null) {
            fileSegment = segment;
        } else {
            fileSegment = new FileSegment();
        }
    }

    public boolean add(FileDescriptor fileDescriptor) {
        LOG.info("adding file to index: {}, segment: {}", fileDescriptor.getLocation(), fileDescriptor.getIndexSegmentId());
        boolean add = fileSegment.getIndexedFiles().add(fileDescriptor);
        persist();
        return add;
    }

    public boolean remove(FileDescriptor fileDescriptor) {
        LOG.info("dropping file from index: {}", fileDescriptor.getLocation());
        boolean remove = fileSegment.getIndexedFiles().remove(fileDescriptor);
        persister.remove(fileDescriptor.getIndexSegmentId());
        persist();
        return remove;
    }

    public boolean update(FileDescriptor fileDescriptor) {
        LOG.info("updating file from index: {}", fileDescriptor.getLocation());
        boolean update = fileSegment.getIndexedFiles().remove(fileDescriptor)
                && fileSegment.getIndexedFiles().add(fileDescriptor);
        persist();
        return update;
    }

    public Set<String> getIndexedFiles() {
        return fileSegment.getIndexedFiles().stream().map(FileDescriptor::getLocation).collect(Collectors.toSet());
    }

    public Set<FileDescriptor> getIndexedFilDescriptors() {
        return fileSegment.getIndexedFiles();
    }

    private void persist() {
        persister.save(fileSegment);
    }
}
