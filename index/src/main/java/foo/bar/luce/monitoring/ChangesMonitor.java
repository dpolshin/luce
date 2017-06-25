package foo.bar.luce.monitoring;

import foo.bar.luce.FileRegistry;
import foo.bar.luce.Indexer;
import foo.bar.luce.model.FileDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.sun.nio.file.SensitivityWatchEventModifier.HIGH;
import static java.nio.file.StandardWatchEventKinds.*;


public class ChangesMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(ChangesMonitor.class);
    private static final WatchEvent.Kind[] EVENT_KINDS = {ENTRY_DELETE, ENTRY_MODIFY, ENTRY_CREATE, OVERFLOW};

    private ExecutorService executorService;
    private BlockingQueue<Path> changesQueue = new LinkedBlockingQueue<>();
    private Map<WatchKey, Path> keys = new HashMap<>();
    private WatchService watchService;

    private Collection<FileDescriptor> watchedFiles;
    private Indexer indexer;


    //todo: add rescan on startup
    public ChangesMonitor(FileRegistry fileRegistry, Indexer indexer) {
        LOG.info("starting file changes monitor");
        watchedFiles = fileRegistry.getIndexedFilDescriptors();
        this.indexer = indexer;

        executorService = new ThreadPoolExecutor(1, 100, 600L, TimeUnit.SECONDS, new SynchronousQueue<>());

        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        daemonize(this::drain);
        daemonize(this::processEvents);

        for (FileDescriptor fileDescriptor : fileRegistry.getIndexedFilDescriptors()) {
            String location = fileDescriptor.getLocation();
            Path path = new File(location).toPath();
            register(path);
        }
    }

    private void daemonize(Runnable target) {
        Thread daemon = new Thread(target);
        daemon.setDaemon(true);
        daemon.start();
    }

    public void register(Path file) {
        try {
            Path path = file.getParent();
            WatchKey key = path.register(watchService, EVENT_KINDS, HIGH);
            keys.put(key, path);
        } catch (IOException e) {
            LOG.error("registering file to watch service failed", e);
        }
    }


    private void drain() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                Path path = changesQueue.take();

                //to avoid notifier queue choking
                //calculate file checksum and re-index in thread pool
                executorService.submit(() -> {
                    FileDescriptor newFileDescriptor = new FileDescriptor(path.toFile());
                    List<FileDescriptor> oldFileDescriptors = watchedFiles.parallelStream().filter(e -> e.equals(newFileDescriptor)).collect(Collectors.toList());

                    if (oldFileDescriptors.size() != 0) {
                        Long oldHash = oldFileDescriptors.get(0).getHash();
                        if (!oldHash.equals(newFileDescriptor.getHash())) {
                            LOG.info("found changes in file: " + newFileDescriptor.getLocation());
                            indexer.index(newFileDescriptor);
                        }
                    }
                });

            } catch (InterruptedException e) {
                LOG.error("changes monitor thread interrupted", e);
            }
        }
    }

    void processEvents() {
        while (true) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                LOG.warn("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                Path name = (Path) event.context();
                Path child = dir.resolve(name);

                FileDescriptor fileDescriptor = new FileDescriptor(child.toString());
                if (watchedFiles.contains(fileDescriptor)) {
                    changesQueue.add(child);
                } else {
                    boolean valid = key.reset();
                    if (!valid) {
                        keys.remove(key);
                        LOG.info("removing file {} from monitoring", fileDescriptor.getLocation());
                    }
                }
            }
        }
    }
}
