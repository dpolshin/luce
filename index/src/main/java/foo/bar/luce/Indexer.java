package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.Position;
import foo.bar.luce.model.Token;

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
    private IndexRegistry indexRegistry;

    public Indexer(IndexRegistry indexRegistry) {
        this.indexRegistry = indexRegistry;
    }


    public void index(File file) {
        try {
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
            FileDescriptor fileDescriptor = new FileDescriptor(file);
            indexRegistry.addOrUpdate(fileDescriptor, new IndexSegment(fileDescriptor, index));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
