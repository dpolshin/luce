package foo.bar.luce.util;

import foo.bar.luce.model.FileDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Checksum;

/**
 * Utility class for file-related stuff.
 */
public class FileUtil {

    /**
     * Calculate Adler32 file hash.
     *
     * @param fileDescriptor file
     * @return hash
     */
    public static long hash(FileDescriptor fileDescriptor) {
        String fileName = fileDescriptor.getLocation();

        try (FileInputStream inputStream = new FileInputStream(fileName)) {
            Adler32 adlerChecksum = new Adler32();
            CheckedInputStream cinStream = new CheckedInputStream(inputStream, adlerChecksum);

            byte[] b = new byte[128];

            //noinspection StatementWithEmptyBody
            while (cinStream.read(b) >= 0) {
                //nop
            }

            return cinStream.getChecksum().getValue();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }


    public static String hash(String string) {
        byte[] bytes = string.getBytes();
        Checksum checksum = new Adler32();
        checksum.update(bytes, 0, bytes.length);
        return String.valueOf(checksum.getValue());
    }


    public static CharSequence fromFile(File file) throws IOException {
        try (FileInputStream stream = new FileInputStream(file);
             FileChannel channel = stream.getChannel()) {

            ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, (int) channel.size());
            return Charset.forName("UTF-8").newDecoder().decode(buffer);
        }
    }
}
