package foo.bar.luce.model;

/**
 * Class representing single token emitted by text tokenizer.
 * A word and position of a word within a file.
 */
public class Token {
    private String token;
    private Position position;

    public Token(String token, int startOffset, int endOffset) {
        this.token = token;
        this.position = new Position(startOffset, endOffset);
    }

    public String getToken() {
        return token;
    }

    public int getStartOffset() {
        return position.getStart();
    }

    public int getEndOffset() {
        return position.getEnd();
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return token + ", " + position.getStart() + ", " + position.getEnd();
    }
}