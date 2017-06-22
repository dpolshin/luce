import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sun.nio.file.SensitivityWatchEventModifier.LOW;
import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Example to watch a directory (or tree) for changes to files.
 */

public class WatchDir {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private Map<Path, List<Path>> files;

    /**
     * Creates a WatchService and registers the given directory
     */
    WatchDir() throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        files = new HashMap<>();

    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }


    public static void main(String[] args) throws IOException {
        //Path dir = Paths.get(args[0]);
        Path path = new File("/home/pacman/downloads/test.txt").toPath();
        WatchDir watchDir = new WatchDir();
        watchDir.processEvents();
        watchDir.register(path);
    }


    /**
     * Register the file with the WatchService
     */
    private void register(Path path) throws IOException {
        WatchEvent.Kind[] eventKinds = {ENTRY_DELETE, ENTRY_MODIFY};
        WatchKey key = path.register(watcher, eventKinds, LOW);

        //trace changes
        Path prev = keys.get(key);
        if (prev == null) {
            System.out.format("register: %s\n", path);
        } else {
            if (!path.equals(prev)) {
                System.out.format("update: %s -> %s\n", prev, path);
            }
        }

        keys.put(key, path);
    }


    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() {
        for (; ; ) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                // print out event
                System.out.format("%s: %s\n", event.kind().name(), child);

            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
}
