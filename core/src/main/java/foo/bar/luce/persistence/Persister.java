package foo.bar.luce.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Service operating with persistent objects stored on the filesystem.
 */
public class Persister {
    private static final Logger LOG = LoggerFactory.getLogger(Persister.class);

    private static final String DATA_DIR = ".data";
    private final File dataDir;

    public Persister() {
        String work = System.getProperty("user.dir");
        File dir = new File(work + "/" + DATA_DIR);
        if (!dir.exists()) {
            boolean mkdir = dir.mkdir();
            if (!mkdir) {
                throw new RuntimeException("can't create data directory");
            }
        }
        this.dataDir = dir;
    }


    public <T> T load(String id) {
        File file = new File(dataDir, id);

        try (FileInputStream inStream = new FileInputStream(file);
             ObjectInputStream objectInputFile = new ObjectInputStream(inStream)) {

            //noinspection unchecked
            T object = (T) objectInputFile.readObject();

            LOG.info("loaded object with id: {}", id);

            return object;
        } catch (Exception e) {
            LOG.warn("object with id: {} not found", id);
            return null;
        }
    }


    public <T extends Persistable> void save(T persistable) throws RuntimeException {
        String id = persistable.getId();

        File file = new File(dataDir, id);
        if (!file.exists()) {
            boolean created;
            try {
                created = file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("can't create data file", e);
            }
            if (!created) {
                throw new RuntimeException("can't create data file");
            }
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(persistable);
        } catch (Exception e) {
            throw new RuntimeException("can't write data file", e);
        }
    }

    public <T extends Persistable> void remove(String id) throws RuntimeException {
        File file = new File(dataDir, id);
        if (!file.exists()) {
            LOG.info("file id: {} transient or already removed", id);
            return;
        }

        if (!file.delete()) {
            LOG.warn("can't remove file id: {}", id);
        }
    }

}
