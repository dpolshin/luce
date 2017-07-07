package foo.bar.luce.model;

public class IndexingResult {
    public enum Code {
        ok("Added to index"),
        fail("Failed"),
        duplicate("Already indexed"),
        unsupported("Unsupported or corrupt");

        public final String label;

        Code(String label) {
            this.label = label;
        }
    }

    private Code code;
    private String path;

    public IndexingResult(Code code, String path) {
        this.code = code;
        this.path = path;
    }

    public Code getCode() {
        return code;
    }

    public String getPath() {
        return path;
    }
}
