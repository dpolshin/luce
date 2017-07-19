package foo.bar.luce.util;

import foo.bar.luce.model.FileDescriptor;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

import static foo.bar.luce.Constants.DEFAULT_CHARSET;


/**
 * Convert Reader into Stream of Tokens.
 */
public class CheckedFileCharReaderSpliterator extends CharReaderSpliterator {
    private CheckedInputStream checkedInputStream;
    private FileDescriptor fileDescriptor;

    public CheckedFileCharReaderSpliterator(FileDescriptor fileDescriptor) throws FileNotFoundException {
        super();

        this.fileDescriptor = fileDescriptor;

        CharsetDecoder decoder = Charset.forName(DEFAULT_CHARSET).newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);

        FileInputStream in = new FileInputStream(fileDescriptor.getFile());
        checkedInputStream = new CheckedInputStream(in, new Adler32());
        this.reader = new BufferedReader(new InputStreamReader(checkedInputStream, decoder));
    }


    @Override
    public void close() throws IOException {
        long checksum = checkedInputStream.getChecksum().getValue();
        fileDescriptor.setDigest(checksum);
        super.close();
    }
}
