package foo.bar.luce;

public interface Persistable<T> {
    String getId();

    boolean persist();

    T restore(String id);
}
