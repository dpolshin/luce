package foo.bar.luce.model;

import foo.bar.luce.util.FileUtil;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class representing exact file state that was added for indexing.
 */
public class FileDescriptor implements Comparable<FileDescriptor>, Serializable {
    private static final long serialVersionUID = 6277827909098651964L;
    private transient File file; //nullable;
    private String location; //absolute path;
    private long lastModified;
    private long digest; //Adler32 file sum;
    private String indexSegmentPrefix; //Adler32 absolutePath string sum;
    private int chunks = 0;


    public FileDescriptor(String location) {
        this.location = location;
        this.indexSegmentPrefix = FileUtil.hash(location);
    }


    public FileDescriptor(File file) {
        this(file.getAbsolutePath());
        this.lastModified = file.lastModified();
        this.file = file;
    }


    public File getFile() {
        return file;
    }

    public String getLocation() {
        return location;
    }

    public void setDigest(long digest) {
        this.digest = digest;
    }

    public long getDigest() {
        return digest;
    }

    public long getLastModified() {
        return lastModified;
    }

    public List<String> getIndexSegmentIds() {
        ArrayList<String> chunkList = new ArrayList<>();
        for (int i = 0; i <= chunks; i++) {
            chunkList.add(indexSegmentPrefix + "." + i);
        }
        return chunkList;
    }

    public String addChunk() {
        chunks++;
        return indexSegmentPrefix + "." + chunks;
    }

    @Override
    public int compareTo(FileDescriptor o) {
        return location.compareTo(o.location);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileDescriptor that = (FileDescriptor) o;
        return Objects.equals(location, that.location);
    }


    @Override
    public int hashCode() {
        return Objects.hash(location);
    }
}
