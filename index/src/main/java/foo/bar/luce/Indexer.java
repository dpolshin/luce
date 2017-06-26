package foo.bar.luce;

import foo.bar.luce.index.Analyzer;
import foo.bar.luce.index.WordTokenizer;
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
import java.util.stream.Stream;


/**
 * Service responsible of maintaining a registry of known files and indexing new files and file updates.
 */
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

            //todo: research way to lazily create source from file, without reading whole into memory.
            WordTokenizer tokenizer = new WordTokenizer(FileUtil.fromFile(file));
            Stream<Token> rawTokenString = tokenizer.stream();
            Analyzer analyzer = new Analyzer(rawTokenString);
            Stream<Token> tokenStream = analyzer.analyze();

            Map<String, List<Position>> index = new HashMap<>();

            tokenStream.forEach(t -> saveToIndex(t, index));

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

    private void saveToIndex(Token token, Map<String, List<Position>> index) {
        String tokenText = token.getToken();

        List<Position> indexEntry = index.get(tokenText);
        if (indexEntry != null) {
            indexEntry.add(token.getPosition());
        } else {
            LinkedList<Position> positions = new LinkedList<>();
            positions.add(token.getPosition());
            index.put(tokenText, positions);
        }
    }
}
