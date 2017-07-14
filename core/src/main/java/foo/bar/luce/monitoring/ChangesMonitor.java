package foo.bar.luce.monitoring;

import foo.bar.luce.FileRegistry;
import foo.bar.luce.Indexer;
import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.util.FileUtil;
import foo.bar.luce.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.*;


/**
 * Notify index when watched file contents changed.
 */
public class ChangesMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(ChangesMonitor.class);
    private static final WatchEvent.Kind[] EVENT_KINDS = {ENTRY_DELETE, ENTRY_MODIFY, ENTRY_CREATE, OVERFLOW};

    private ExecutorService executorService;
    private BlockingQueue<Pair<Path, WatchEvent.Kind>> changesQueue = new LinkedBlockingQueue<>();
    private Map<WatchKey, Path> keys = new HashMap<>();
    private WatchService watchService;

    private Collection<FileDescriptor> watchRoots;
    private Indexer indexer;
    private FileRegistry fileRegistry;


    public ChangesMonitor(FileRegistry fileRegistry, Indexer indexer) {
        LOG.info("starting file changes monitor");
        this.indexer = indexer;
        this.fileRegistry = fileRegistry;

        watchRoots = fileRegistry.getWatchRootFilDescriptors();
        executorService = new ThreadPoolExecutor(1, 100, 600L, TimeUnit.SECONDS, new SynchronousQueue<>());

        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        daemonize(this::drain);
        daemonize(this::dispatch);

        for (FileDescriptor fileDescriptor : watchRoots) {
            String location = fileDescriptor.getLocation();
            File file = new File(location);

            if (file.exists()) {
                Path path = file.toPath();

                if (!file.isDirectory()) {
                    //todo;
                    //registerFile(path);
                } else {
                    registerRecursively(path);
                }
            } else {
                LOG.error("Registered file no longer exists: {}", location);
                fileRegistry.remove(fileDescriptor);
            }
        }
    }

    private void daemonize(Runnable target) {
        Thread daemon = new Thread(target);
        daemon.setDaemon(true);
        daemon.start();
    }


    private void drain() {
        Thread.currentThread().setName("ChangesMonitor.drain");
        LOG.info("starting watch service event processor thread");
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                Pair<Path, WatchEvent.Kind> event = changesQueue.take();

                //to avoid dispatcher queue choking
                //calculate file checksum and re-index in thread pool
                executorService.submit(() -> {
                    FileDescriptor fileDescriptor = new FileDescriptor(event.getLeft().toFile());
                    WatchEvent.Kind eventType = event.getRight();
                    File file = fileDescriptor.getFile();


                    if (eventType.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                        LOG.info("New file was added to filesystem: {} ", fileDescriptor.getLocation());

                        if (file.isDirectory()) {
                            Path path = file.toPath();
                            addOrUpdateIndexRecursively(path);
                        } else {
                            addOrUpdateIndex(fileDescriptor);
                        }
                    }

                    if (eventType.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                        LOG.info("New file was modified on filesystem: {} ", fileDescriptor.getLocation());

                        FileDescriptor currentVersion = getCurrentVersion(fileDescriptor);
                        if (currentVersion != null) {
                            long newTimeStamp = fileDescriptor.getLastModified();
                            long currentTimeStamp = currentVersion.getLastModified();

                            if (newTimeStamp != currentTimeStamp) {
                                long newDigest = FileUtil.hash(fileDescriptor);
                                long currentDigest = fileDescriptor.getDigest();

                                if (newDigest != currentDigest) {
                                    addOrUpdateIndex(fileDescriptor);
                                }
                            }
                        }
                    }

                    if (eventType.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                        LOG.info("New file was removed from filesystem: {} ", fileDescriptor.getLocation());
                        fileRegistry.remove(fileDescriptor);
                    }
                });

            } catch (InterruptedException e) {
                LOG.error("changes monitor thread interrupted", e);
            }
        }
    }


    /**
     * Process the given directory, and all its sub-directories, with supplied consumer
     */
    private void addOrUpdateIndexRecursively(Path path) {
        // process directory and sub-directories
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    register(path);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    if (!path.toFile().isDirectory()) {
                        addOrUpdateIndex(new FileDescriptor(path.toFile()));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOG.error("processing file failed", e);
        }
    }

    private void registerRecursively(Path dir) {
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    register(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LOG.error("processing file failed", e);
        }
    }

    /**
     * Register the given file with the WatchService.
     */
    private void register(Path dir) {
        try {
            WatchKey key = dir.register(watchService, EVENT_KINDS);
            keys.put(key, dir);
        } catch (IOException e) {
            LOG.error("registering file to watch service failed", e);
        }
    }


    private FileDescriptor getCurrentVersion(FileDescriptor fileDescriptor) {
        List<FileDescriptor> oldFileDescriptors = fileRegistry.getIndexedFilDescriptors()
                .parallelStream()
                .filter(e -> e.equals(fileDescriptor)).collect(Collectors.toList());

        if (oldFileDescriptors.size() != 0) {
            return oldFileDescriptors.get(0);
        }
        return null;
    }


    private void addOrUpdateIndex(FileDescriptor fileDescriptor) {
        try {
            indexer.index(fileDescriptor);
            fileRegistry.addOrUpdate(fileDescriptor);
        } catch (Exception e) {
            LOG.error("Cant update index for file: " + fileDescriptor.getLocation(), e);
        }
    }

    private void addIndexDir(FileDescriptor fileDescriptor) {
        File file = fileDescriptor.getFile();


    }


    /**
     * Get keys queued to the watcher and dispatch to processor queue.
     */
    void dispatch() {
        Thread.currentThread().setName("ChangesMonitor.dispatch");
        LOG.info("start watch service event dispatcher thread");

        while (true) {
            WatchKey key;

            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                LOG.error("thread interrupted", e);
                return;
            }

            try {
                Path dir = keys.get(key);
                if (dir == null) {
                    LOG.warn("WatchKey not recognized!! {}", key);
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind eventKind = event.kind();

                    if (eventKind == OVERFLOW) {
                        continue;
                    }

                    Path eventPath = (Path) event.context();
                    Path file = dir.resolve(eventPath);

                    LOG.debug("{}: {}", event.kind().name(), file);
                    changesQueue.add(new Pair<>(file, event.kind()));
                }

                // reset key and remove from set if directory no longer accessible
                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);
                    LOG.debug("remove key {}", key);
                    //todo: remove from index
                    // all directories are inaccessible
                    if (keys.isEmpty()) {
                        break;
                    }
                }
            } catch (RuntimeException e) {
                LOG.error("Exception in ChangesMonitor dispatcher thread", e);
            }
        }
    }
}
