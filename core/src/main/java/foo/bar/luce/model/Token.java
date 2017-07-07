package foo.bar.luce.model;

/**
 * Class representing single token emitted by text tokenizer.
 * A word and position of a word within input.
 */
public class Token {
    private String token;
    private int position;

    public Token(String token, int position) {
        this.token = token;
        this.position = position;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
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
