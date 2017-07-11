package foo.bar.luce.index;

import foo.bar.luce.Ranker;
import foo.bar.luce.model.SearchResultItem;
import foo.bar.luce.model.Token;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.LinkedHashMap;
import java.util.stream.Stream;

@RunWith(JUnit4.class)
public class RankerTest {

    @Test
    public void testMatchResult() {
        LinkedHashMap<Integer, Character> map = new LinkedHashMap<>();
        map.put(0, 'l');
        map.put(1, 'o');
        map.put(2, 'r');
        map.put(3, 'e');
        map.put(4, 'm');
        map.put(38, 'r');
        map.put(51, 'e');
        map.put(52, 'l');
        map.put(57, 'm');
        map.put(59, 'e');
        map.put(61, 'e');
        map.put(66, 'e');
        map.put(68, 'e');
        map.put(71, 'e');
        map.put(72, 'l');
        map.put(92, 'm');
        map.put(93, 'o');
        map.put(94, 'l');
        map.put(95, 'e');
        map.put(99, 'e');
        map.put(102, 'o');
        map.put(106, 'e');
        map.put(109, 'm');
        map.put(117, 'e');
        map.put(122, 'r');
        map.put(130, 'e');
        map.put(142, 'e');
        map.put(143, 'r');
        map.put(146, 'm');
        map.put(153, 'm');
        map.put(158, 'o');
        map.put(159, 'l');
        map.put(160, 'l');
        map.put(171, 'e');
        map.put(172, 'l');
        map.put(173, 'l');
        map.put(174, 'e');
        map.put(177, 'e');
        map.put(181, 'e');
        map.put(190, 'l');
        map.put(191, 'l');
        map.put(193, 'm');
        map.put(199, 'e');
        map.put(201, 'l');
        map.put(205, 'l');
        map.put(208, 'r');
        map.put(211, 'r');
        map.put(213, 'm');
        map.put(217, 'r');
        map.put(222, 'l');
        map.put(223, 'l');
        map.put(227, 'r');
        map.put(231, 'e');
        map.put(235, 'e');
        map.put(236, 'l');
        map.put(237, 'e');
        map.put(238, 'm');
        map.put(239, 'e');
        map.put(243, 'm');
        map.put(245, 'l');
        map.put(246, 'e');
        map.put(247, 'o');
        map.put(251, 'e');
        map.put(252, 'l');
        map.put(253, 'l');
        map.put(254, 'e');
        map.put(311, 'l');
        map.put(312, 'e');
        map.put(367, 'l');
        map.put(368, 'l');
        map.put(369, 'e');
        map.put(372, 'e');
        map.put(376, 'e');
        map.put(395, 'o');
        map.put(435, 'e');
        map.put(436, 'l');
        map.put(437, 'l');
        map.put(442, 'o');
        map.put(443, 'r');
        map.put(449, 'r');
        map.put(454, 'l');
        map.put(455, 'o');
        map.put(456, 'r');
        map.put(457, 'e');
        map.put(458, 'm');
        map.put(485, 'r');
        map.put(488, 'l');
        map.put(490, 'r');
        map.put(494, 'e');
        map.put(499, 'o');
        map.put(500, 'l');
        map.put(510, 'm');
        map.put(513, 'e');
        map.put(518, 'o');
        map.put(521, 'l');
        map.put(522, 'o');
        map.put(523, 'r');
        map.put(524, 'e');
        map.put(525, 'm');
        map.put(526, 'l');

        Stream<Token<Character>> stream = map.entrySet().stream().map(e -> new Token<>(e.getValue(), e.getKey()));

        Ranker ranker = new Ranker();

        SearchResultItem item = ranker.matchResult("lorem", "any", stream);

        Assert.assertEquals(3, item.getPositions().size());
        Assert.assertEquals(0, item.getPositions().get(0).getPosition());
        Assert.assertEquals(454, item.getPositions().get(1).getPosition());
        Assert.assertEquals(521, item.getPositions().get(2).getPosition());

    }

    @Test
    public void testSingleLetterMatch() {
        LinkedHashMap<Integer, Character> map = new LinkedHashMap<>();
        map.put(0, 's');
        map.put(1, 's');
        map.put(2, 's');
        map.put(3, 's');
        map.put(4, 's');
        map.put(38, 's');
        map.put(51, 's');
        map.put(56, 's');
        map.put(57, 's');
        map.put(500, 's');


        Stream<Token<Character>> stream = map.entrySet().stream().map(e -> new Token<>(e.getValue(), e.getKey()));

        Ranker ranker = new Ranker();

        SearchResultItem item = ranker.matchResult("s", "any", stream);

        Assert.assertEquals(10, item.getPositions().size());
    }
}
