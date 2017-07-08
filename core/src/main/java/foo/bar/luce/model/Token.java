package foo.bar.luce.model;

/**
 * Class representing single token emitted by text tokenizer.
 * A word and position of a word within input.
 */
public class Token<T> {
    private T token;
    private int position;

    public Token(T token, int position) {
        this.token = token;
        this.position = position;
    }

    public T getToken() {
        return token;
    }

    public void setToken(T token) {
        this.token = token;
    }


    public int getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return token + ", " + position;
    }
}
