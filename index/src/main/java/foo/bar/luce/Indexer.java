package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.IndexEntry;
import foo.bar.luce.model.Token;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


/**
 * Service responsible of maintaining a registry of known files and indexing new files and file updates.
 */
//todo: address case sensitivity
public class Indexer {
    private MemoryIndexRepository indexRepository;

    public Indexer(MemoryIndexRepository indexRepository) {
        this.indexRepository = indexRepository;
    }


    public void index(File file) {
        try {
            WordTokenizer tokenizer = new WordTokenizer(FileUtil.fromFile(file));
            Map<String, IndexEntry> index = new HashMap<>();

            Token t = tokenizer.next();

            while (t != null) {

                String tokenText = t.getToken();

                if (tokenText.length() > 1) {
                    IndexEntry indexEntry = index.get(tokenText);
                    if (indexEntry != null) {
                        indexEntry.getTokens().add(t.getPosition());
                    } else {
                        index.put(tokenText, new IndexEntry(t));
                    }
                }

                t = tokenizer.next();
            }
            indexRepository.addOrUpdate(new FileDescriptor(file), index);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
