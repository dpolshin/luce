package foo.bar.luce.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@SuppressWarnings("UnnecessaryBoxing")
@RunWith(JUnit4.class)
public class TestCharUtil {

    @Test
    public void testCache() {
        Character c = new Character('c');

        Character cached = CharUtil.of((int) c);
        Character cached2 = CharUtil.of(99);

        Assert.assertFalse(cached == c);
        Assert.assertTrue(cached == cached2);
    }

    @Test
    public void testToLower() {
        Character c1 = new Character('C');
        Character c2 = new Character('C');

        Assert.assertTrue(CharUtil.toLower(c1) == CharUtil.toLower(c2));

    }
}
