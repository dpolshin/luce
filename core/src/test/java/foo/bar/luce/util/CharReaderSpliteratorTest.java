package foo.bar.luce.util;

import foo.bar.luce.index.TestTokenizer;
import foo.bar.luce.model.Token;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.StringReader;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RunWith(JUnit4.class)
public class CharReaderSpliteratorTest {

    @Test
    public void testSpliterator() {
        StringReader stringReader = new StringReader(TestTokenizer.TEXT);

        CharReaderSpliterator s = new CharReaderSpliterator(stringReader);

        Stream<Token<Character>> stream = StreamSupport.stream(s, false);
        StringBuilder b = new StringBuilder();

        stream.map(Token::getToken)
                .peek(System.out::println)
                .forEach(b::append);

        Assert.assertEquals(TestTokenizer.TEXT, b.toString());
    }
}
