package foo.bar.luce.util;

public class CharUtil {
    private static final Character[] CACHE;
    private static final String ALPHABET =
            "abcdefghijklmnopqrstuvwxyz" +
                    "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "абвгдежзиклмнопрстуфхцчшщьыъэюя" +
                    "АБВГДЕЖЗИКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" +
                    "1234567890" +
                    "!@#$%^&*()_+{}<>?|/ ";

    static {
        CACHE = new Character[1104];
        for (int i = 0; i < ALPHABET.length(); i++) {
            char c = ALPHABET.charAt(i);
            CACHE[c] = c;
        }
    }

    public static Character of(int codePoint) {
        if (codePoint < CACHE.length && CACHE[codePoint] != null) {
            return CACHE[codePoint];
        } else {
            return (char) codePoint;
        }
    }

    public static Character toLower(Character c) {
        //noinspection UnnecessaryUnboxing
        int lower = Character.toLowerCase((int) c.charValue());
        return of(lower);
    }
}
