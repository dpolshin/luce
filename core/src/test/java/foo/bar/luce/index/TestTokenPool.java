package foo.bar.luce.index;


import foo.bar.luce.TokenPool;
import foo.bar.luce.model.Token;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@RunWith(JUnit4.class)
public class TestTokenPool {

    @Test
    public void testMergeLanes() {
        List<Integer> l = Arrays.asList(0, 14, 52, 72, 94, 159, 160, 172, 173, 190, 191, 201, 205, 222, 223, 236, 245, 252, 253, 311, 367, 368, 396, 397, 407, 415, 436, 437, 454, 488, 500, 531);
        List<Integer> o = Arrays.asList(1, 13, 15, 29, 93, 102, 158, 247, 273, 357, 395, 442, 455, 461, 469, 477, 481, 484, 499, 518);
        List<Integer> r = Arrays.asList(2, 16, 38, 122, 143, 208, 211, 217, 227, 274, 279, 330, 443, 449, 456, 482, 485, 490, 527);
        List<Integer> e = Arrays.asList(3, 24, 32, 35, 51, 59, 61, 66, 68, 71, 95, 99, 106, 117, 130, 142, 171, 174, 177, 181, 199, 231, 235, 237, 239, 246, 251, 254, 257, 261, 286, 289, 291, 297, 301, 306, 312, 322, 335, 337, 348, 354, 359, 366, 369, 372, 376, 414, 416, 418, 435, 457, 464, 494, 513, 521, 524, 530);
        List<Integer> m = Arrays.asList(4, 10, 23, 57, 92, 109, 146, 153, 193, 213, 238, 243, 272, 309, 321, 417, 422, 427, 458, 468, 510);

        Set<Object> sorted = new TreeSet<>();
        sorted.addAll(l);
        sorted.addAll(o);
        sorted.addAll(r);
        sorted.addAll(e);
        sorted.addAll(m);
        Object[] treeSortedArray = sorted.toArray();

        StringBuilder b = new StringBuilder();
        for (Object oo : treeSortedArray) {
            b.append(oo).append(" ");
        }

        String expected = b.toString();

        TokenPool<Character> pool = new TokenPool<>();
        pool.addSwimlane(r.stream().map(i -> new Token<>('r', i)));
        pool.addSwimlane(l.stream().map(i -> new Token<>('l', i)));
        pool.addSwimlane(o.stream().map(i -> new Token<>('o', i)));
        pool.addSwimlane(e.stream().map(i -> new Token<>('e', i)));
        pool.addSwimlane(m.stream().map(i -> new Token<>('m', i)));

        List<Token<Character>> tokenList = pool.stream().collect(Collectors.toList());

        StringBuilder bb = new StringBuilder();
        tokenList.forEach(t -> bb.append(t.getPosition()).append(" "));
        String actual = bb.toString();

        Assert.assertEquals(expected, actual);
    }
}
