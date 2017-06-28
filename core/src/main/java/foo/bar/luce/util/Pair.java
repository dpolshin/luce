package foo.bar.luce.util;

public class Pair<Left, Right> {
    private Left left;
    private Right right;

    public Pair(Left left, Right right) {
        this.left = left;
        this.right = right;
    }

    public Left getLeft() {
        return left;
    }

    public Right getRight() {
        return right;
    }
}
