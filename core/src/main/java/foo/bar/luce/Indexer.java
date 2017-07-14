package foo.bar.luce;

import foo.bar.luce.index.Analyzer;
import foo.bar.luce.index.ToLowerCaseCharFilter;
import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.IndexSegment;
import foo.bar.luce.model.Token;
import foo.bar.luce.util.CharReaderSpliterator;
import foo.bar.luce.util.CheckedFileCharReaderSpliterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Service responsible of maintaining a registry of known files and indexing new files and file updates.
 */
public class Indexer {
    private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

    private IndexRegistry indexRegistry;
    private FileRegistry fileRegistry;

    private Analyzer<Character> analyzer = new Analyzer<>(new ToLowerCaseCharFilter());


    public Indexer(IndexRegistry indexRegistry, FileRegistry fileRegistry) {
        this.indexRegistry = indexRegistry;
        this.fileRegistry = fileRegistry;
    }


    public void index(FileDescriptor fileDescriptor) throws Exception {
        final AtomicReference<Integer> counter = new AtomicReference<>(0);
        final IndexSegment[] currentChunk = {new IndexSegment(fileDescriptor.getIndexSegmentIds().get(0), new HashMap<>())};


        try (CharReaderSpliterator charReaderSpliterator = new CheckedFileCharReaderSpliterator(fileDescriptor)) {

            charReaderSpliterator.stream()
                    .flatMap(analyzer::analyze)
                    .peek(t -> counter.getAndUpdate(i -> i + 1))

                    .forEach(t -> {
                        boolean newChunk = counter.get() % Constants.MAX_TOKENS_PER_CHUNK == 0;

                        if (newChunk) {

                            indexRegistry.addOrUpdate(fileDescriptor, currentChunk[0]);
                            fileRegistry.addOrUpdate(fileDescriptor);
                            LOG.info("adding index chunk for file: {}, total tokens: {} unique tokens: {}",
                                    fileDescriptor.getLocation(), counter.get(), currentChunk[0].getSegment().keySet().size());

                            String chunkId = fileDescriptor.addChunk();
                            currentChunk[0] = new IndexSegment(chunkId, new HashMap<>());
                        }

                        IndexSegment chunk = currentChunk[0];
                        saveToIndex(t, chunk);


                    });
            indexRegistry.addOrUpdate(fileDescriptor, currentChunk[0]);
            fileRegistry.addOrUpdate(fileDescriptor);
            LOG.info("indexing completed. file: {}, total tokens: {} unique tokens: {}",
                    fileDescriptor.getLocation(), counter.get(), currentChunk[0].getSegment().keySet().size());

        }
    }


    private void saveToIndex(Token<Character> token, IndexSegment indexSegment) {
        Map<Character, List<Integer>> index = indexSegment.getSegment();

        Character tokenText = token.getToken();

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
