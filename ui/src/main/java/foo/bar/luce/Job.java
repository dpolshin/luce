package foo.bar.luce;

import javax.swing.*;

public abstract class Job<T> extends SwingWorker<String, T> {
    private final long id;
    private final String description;
    private Runnable disposer;

    public Job(String description) {
        this.id = System.currentTimeMillis();
        this.description = description;
    }

    public final long getId() {
        return id;
    }

    public final String getDescription() {
        return description;
    }

    final void onDispose(Runnable disposer) {
        this.disposer = disposer;
    }

    public void onComplete() {

    }

    @Override
    protected final void done() {
        onComplete();
        disposer.run();
    }
}
