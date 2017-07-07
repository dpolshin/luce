package foo.bar.luce.index;

import foo.bar.luce.model.Token;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(JUnit4.class)
public class AnalyzerTest {
    @Test
    public void testRegisterFilering() throws Exception {
        List<Token> filteredToken = getFilteredToken("CAPS");
        Assert.assertEquals("caps", filteredToken.get(0).getToken());
    }

    @Test
    @Ignore
    //length filter was asked to ignore
    public void testLengthFiltering() throws Exception {
        List<Token> filteredToken = getFilteredToken("ca");
        Assert.assertEquals(0, filteredToken.size());
    }


    @Test
    public void testStopFiltering() throws Exception {
        List<Token> filteredToken = getFilteredToken("then");
        Assert.assertEquals(0, filteredToken.size());
    }


    private List<Token> getFilteredToken(String source) {
        List<Token> tokens = Collections.singletonList(new Token(source, 0));
        Analyzer analyzer = new Analyzer();
        return tokens.stream().flatMap(analyzer::analyze).collect(Collectors.toList());
    }
}
