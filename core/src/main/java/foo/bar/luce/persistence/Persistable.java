package foo.bar.luce.persistence;

import java.io.Serializable;

/**
 * Basic interface for class that may be stored into persistent disk storage.
 */
public interface Persistable extends Serializable {
    String getId();
}
