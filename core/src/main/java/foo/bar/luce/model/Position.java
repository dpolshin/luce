package foo.bar.luce.model;

import java.io.Serializable;

/**
 * Class representing position of a term within a file.
 */
public class Position implements Serializable, Comparable<Position> {
    private int start;
    private int end;

    public Position(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public int compareTo(Position o) {
        return Integer.compare(this.start, o.start);
    }
}