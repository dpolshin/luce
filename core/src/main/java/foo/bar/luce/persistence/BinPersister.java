package foo.bar.luce.persistence;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import foo.bar.luce.Constants;
import foo.bar.luce.model.IndexSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service operating with persistent objects stored on the filesystem.
 */
public class BinPersister {
    private static final Logger LOG = LoggerFactory.getLogger(BinPersister.class);
    private final File dataDir;

    public BinPersister() {
        String work = System.getProperty("user.dir");
        File dir = new File(work + "/" + Constants.DATA_DIR);
        if (!dir.exists()) {
            boolean mkdir = dir.mkdir();
            if (!mkdir) {
                throw new RuntimeException("can't create data directory");
            }
        }
        this.dataDir = dir;
    }

    //todo;
//    public IndexHeader loadHeader(String id) {
//    }


    //todo;
//    public IndexSegment load(String id) {
//
//        //read header;
//        File file = new File(dataDir, id);
//        try(
//        FileInputStream inputStream = new FileInputStream(file);
//        DataInputStream is = new DataInputStream(inputStream)) {
//
//            int headerLength = is.readInt();
//            int[] readheader = new int[headerLength];
//            readheader[0] = headerLength;
//
//
//            for (int hi = 1; hi < headerLength; hi++) {
//                readheader[hi] = is.readInt();
//            }
//
//
//            Map<Character, HeaderEntry> readHeaderMap = new HashMap<>();
//
//            //parse header
//            int charsInHeader = (headerLength - 1) / 4;
//
//            int headerOffset = 1;
//            for (int ch = 0; ch < charsInHeader; ch++) {
//                char c = (char) readheader[headerOffset];
//                int chCount = readheader[headerOffset + 1];
//                int chStart = readheader[headerOffset + 2];
//                int chEnd = readheader[headerOffset + 3];
//
//                HeaderEntry headerEntry = new HeaderEntry(c, chCount, chStart, chEnd);
//                readHeaderMap.put(c, headerEntry);
//
//                headerOffset = headerOffset + 4;
//
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("can't create data file", e);
//        }
//    }

    //todo;
//    public Stream<Integer> stream(String id, Character token) {
//    }

    private File validate(Persistable persistable) {
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
        return file;
    }


    public void save(IndexSegment indexSegment) {
        File file = validate(indexSegment);
        Map<Character, List<Integer>> segment = indexSegment.getSegment();
        Set<Character> characters = segment.keySet();


        int alphabetSize = characters.size();

        int headerSize = alphabetSize * 4 + 1;
        int[] header = new int[headerSize];
        header[0] = headerSize;


        try (OutputStream outputStream = new FileOutputStream(file);
             OutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
             DataOutputStream stream = new DataOutputStream(bufferedOutputStream)) {

            //calculate header
            int headerPosition = 1;
            int indexPosition = headerSize + 1;

            Set<Map.Entry<Character, List<Integer>>> entries = segment.entrySet();

            for (Map.Entry<Character, List<Integer>> e : entries) {
                Character c = e.getKey();
                List<Integer> indexPositions = e.getValue();

                //header entry
                int size = indexPositions.size();
                header[headerPosition] = (int) c;
                header[headerPosition + 1] = size;
                header[headerPosition + 2] = indexPosition;
                header[headerPosition + 3] = indexPosition + size;

                indexPosition = indexPosition + size;
                headerPosition = headerPosition + 4;
            }

            //save header
            for (int i = 0; i < headerSize; i++) {
                stream.writeInt(header[i]);
            }

            //write data
            for (Map.Entry<Character, List<Integer>> e : entries) {
                for (int i : e.getValue()) {
                    stream.writeInt(i);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("can't write data file", e);
        }
    }


    static class IndexHeader {
        private List<HeaderEntry> headers;

        public IndexHeader(List<HeaderEntry> headers) {
            this.headers = headers;
        }

        public List<HeaderEntry> getHeaders() {
            return headers;
        }
    }


    static class HeaderEntry {
        char character;
        int count;
        int start;
        int end;

        public HeaderEntry(char character, int count, int start, int end) {
            this.character = character;
            this.count = count;
            this.start = start;
            this.end = end;
        }

        public char getCharacter() {
            return character;
        }

        public int getCount() {
            return count;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }
}
