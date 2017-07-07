package foo.bar.luce.index;

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
            "vowels, 2\n" +
                    "అఆఇఈఉఊఋఌఎఏఐఒఓఔౠౡ, 11\n" +
                    "consonants, 30\n" +
                    "కఖగఘఙచఛజఝఞటఠడఢణతథదధనపఫబభమయరఱలళవశషసహ, 43\n" +
                    "signsAndPunctuations, 81\n" +
                    "క, 104\n" +
                    "క, 106\n" +
                    "క, 108\n" +
                    "క, 110\n" +
                    "క, 112\n" +
                    "క, 114\n" +
                    "క, 116\n" +
                    "క, 118\n" +
                    "క, 120\n" +
                    "క, 122\n" +
                    "క, 124\n" +
                    "క, 126\n" +
                    "క, 128\n" +
                    "క, 130\n" +
                    "క, 132\n" +
                    "క, 134\n" +
                    "క, 136\n" +
                    "క, 138\n" +
                    "క, 140\n" +
                    "symbolsAndNumerals, 145\n" +
                    "engChinesStr, 179\n" +
                    "ABC導字會, 194\n";


    @Test
    public void testTokenizer() throws Exception {
        WordTokenizer tokenizer = new WordTokenizer(TEXT);

        StringBuilder sb = new StringBuilder();
        tokenizer.stream().forEach(t -> sb.append(t.toString()).append("\n"));

        String result = sb.toString();
        Assert.assertEquals(EXPECTED, result);
    }
}
