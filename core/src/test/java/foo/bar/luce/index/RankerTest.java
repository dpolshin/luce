package foo.bar.luce.index;

import foo.bar.luce.Ranker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RankerTest {

    @Test
    public void testDistance() {
        Ranker r = new Ranker();
        Assert.assertEquals(0, r.distance("", ""));
        Assert.assertEquals(1, r.distance("a", ""));
        Assert.assertEquals(1, r.distance("A", "a"));
        Assert.assertEquals(2, r.distance("a b c", "a c b"));
        Assert.assertEquals(12, r.distance("first second", "second first"));
        Assert.assertEquals(4, r.distance("top", "bottom"));
        Assert.assertEquals(15, r.distance("hello dolly", "goodbye, blue sky"));
        Assert.assertEquals(6, r.distance("птица птица", "птица решает"));
    }
}
