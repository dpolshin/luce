package foo.bar.luce.model;

import foo.bar.luce.FileUtil;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

/**
 * Class representing exact file state that was added for indexing.
 */
public class FileDescriptor implements Comparable<FileDescriptor>, Serializable {
    private transient File file; //may be null;
    private long hash; //Adler32 sum;
    private String location; //absolute path;
    private String indexSegmentId;


    public FileDescriptor(String location) {
        this.location = location;
        this.indexSegmentId = FileUtil.hash(location);
    }


    public FileDescriptor(File file) {
        this(file.getAbsolutePath());
        this.file = file;
        this.hash = FileUtil.hash(this);
    }


    public File getFile() {
        return file;
    }

    public String getLocation() {
        return location;
    }

    public long getHash() {
        return hash;
    }

    public String getIndexSegmentId() {
        return indexSegmentId;
    }

    @Override
    public int compareTo(FileDescriptor o) {
        return location.compareTo(o.location);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileDescriptor that = (FileDescriptor) o;
        return Objects.equals(location, that.location);
    }


    @Override
    public int hashCode() {
        return Objects.hash(location);
    }
}
