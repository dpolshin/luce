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

@RunWith(JUnit4.class)
public class AnalyzerTest {

    @Test
    public void testRegisterFilering() throws Exception {
        List<Token<String>> filteredToken = getFilteredToken("CAPS");
        Assert.assertEquals("caps", filteredToken.get(0).getToken());
    }

    private List<Token<String>> getFilteredToken(String source) {
        List<Token<String>> tokens = Collections.singletonList(new Token<>(source, 0));
        Analyzer<String> analyzer = new Analyzer<>(new ToLowerCaseStringFilter());
        return tokens.stream().flatMap(analyzer::analyze).collect(Collectors.toList());
    }
}
