package foo.bar.luce;

import foo.bar.luce.index.Analyzer;
import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.IndexSegment;
import foo.bar.luce.model.Token;
import foo.bar.luce.util.CharReaderSpliterator;
import foo.bar.luce.util.CheckedFileCharReaderSpliterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Service responsible of maintaining a registry of known files and indexing new files and file updates.
 */
public class Indexer {
    private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

    private IndexRegistry indexRegistry;
    private FileRegistry fileRegistry;

    Analyzer analyzer = new Analyzer();


    public Indexer(IndexRegistry indexRegistry, FileRegistry fileRegistry) {
        this.indexRegistry = indexRegistry;
        this.fileRegistry = fileRegistry;
    }


    public void index(FileDescriptor fileDescriptor) throws Exception {
        IndexSegment indexSegment;
        try (CharReaderSpliterator charReaderSpliterator = new CheckedFileCharReaderSpliterator(fileDescriptor)) {
            Map<String, List<Integer>> index = new HashMap<>();

            charReaderSpliterator.stream()
                    .flatMap(analyzer::analyze)
                    .forEach(t -> saveToIndex(t, index));

            indexSegment = new IndexSegment(fileDescriptor, index);
        }
        //noinspection ConstantConditions
        if (indexSegment != null) {
            indexRegistry.addOrUpdate(fileDescriptor, indexSegment);
            fileRegistry.addOrUpdate(fileDescriptor);
            LOG.info("indexing completed. file: {}, unique tokens: {}", fileDescriptor.getLocation(), indexSegment.getSegment().keySet().size());
        }
    }


    private void saveToIndex(Token token, Map<String, List<Integer>> index) {
        String tokenText = token.getToken();

        List<Integer> indexEntry = index.get(tokenText);
        if (indexEntry != null) {
            indexEntry.add(token.getPosition());
        } else {
            List<Integer> Integers = new ArrayList<>();
            Integers.add(token.getPosition());
            index.put(tokenText, Integers);
        }
    }
}
