package foo.bar.luce;

import foo.bar.luce.model.Token;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestTokenizer {

    public static final String TEXT =
            "  vowels = అఆఇఈఉఊఋఌఎఏఐఒఓఔౠౡ\n"
                    + "  consonants = కఖగఘఙచఛజఝఞటఠడఢణతథదధనపఫబభమయరఱలళవశషసహ\n"
                    + "  signsAndPunctuations = కఁకంకఃకాకికీకుకూకృకౄకెకేకైకొకోకౌక్కౕకౖ\n"
                    + "  symbolsAndNumerals = ౦౧౨౩౪౫౬౭౮౯\n"
                    + "  engChinesStr = ABC導字會\n";

    public static final String EXPECTED =
            "vowels, 2, 8\n"
                    + "అఆఇఈఉఊఋఌఎఏఐఒఓఔౠౡ, 11, 27\n"
                    + "consonants, 30, 40\n"
                    + "కఖగఘఙచఛజఝఞటఠడఢణతథదధనపఫబభమయరఱలళవశషసహ, 43, 78\n"
                    + "signsAndPunctuations, 81, 101\n"
                    + "క, 104, 105\n"
                    + "క, 106, 107\n"
                    + "క, 108, 109\n"
                    + "క, 110, 111\n"
                    + "క, 112, 113\n"
                    + "క, 114, 115\n"
                    + "క, 116, 117\n"
                    + "క, 118, 119\n"
                    + "క, 120, 121\n"
                    + "క, 122, 123\n"
                    + "క, 124, 125\n"
                    + "క, 126, 127\n"
                    + "క, 128, 129\n"
                    + "క, 130, 131\n"
                    + "క, 132, 133\n"
                    + "క, 134, 135\n"
                    + "క, 136, 137\n"
                    + "క, 138, 139\n"
                    + "క, 140, 141\n"
                    + "symbolsAndNumerals, 145, 163\n"
                    + "engChinesStr, 179, 191\n"
                    + "ABC導字會, 194, 200\n";


    @Test
    public void testTokenizer() throws Exception {
        WordTokenizer tokenizer = new WordTokenizer(TEXT);

        StringBuilder sb = new StringBuilder();

        Token t = tokenizer.next();
        while (t != null) {
            sb.append(t.toString()).append("\n");
            t = tokenizer.next();
        }

        String result = sb.toString();
        Assert.assertEquals(EXPECTED, result);
    }
}
