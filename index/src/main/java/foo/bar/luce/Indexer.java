package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.IndexSegment;
import foo.bar.luce.model.Position;
import foo.bar.luce.model.Token;
import foo.bar.luce.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Service responsible of maintaining a registry of known files and indexing new files and file updates.
 */
//todo: address case sensitivity
public class Indexer {
    private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);
    private final Object lock = new Object();

    private IndexRegistry indexRegistry;
    private FileRegistry fileRegistry;


    public Indexer(IndexRegistry indexRegistry, FileRegistry fileRegistry) {
        this.indexRegistry = indexRegistry;
        this.fileRegistry = fileRegistry;
    }


    public void index(FileDescriptor fileDescriptor) {
        try {
            LOG.info("indexing started...");
            File file = fileDescriptor.getFile();
            WordTokenizer tokenizer = new WordTokenizer(FileUtil.fromFile(file));
            Map<String, List<Position>> index = new HashMap<>();

            Token t = tokenizer.next();

            while (t != null) {

                String tokenText = t.getToken();

                if (tokenText.length() > 1) {
                    List<Position> indexEntry = index.get(tokenText);
                    if (indexEntry != null) {
                        indexEntry.add(t.getPosition());
                    } else {
                        LinkedList<Position> positions = new LinkedList<>();
                        positions.add(t.getPosition());
                        index.put(tokenText, positions);
                    }
                }

                t = tokenizer.next();
            }
            IndexSegment indexSegment = new IndexSegment(fileDescriptor, index);

            synchronized (lock) {
                indexRegistry.addOrUpdate(fileDescriptor, indexSegment);
                fileRegistry.update(fileDescriptor);
            }
            LOG.info("indexing completed. file: {}, unique tokens: {}", fileDescriptor.getLocation(), indexSegment.getSegment().keySet().size());
        } catch (Exception e) {
            LOG.error("indexing failed", e);
        }
    }
}
