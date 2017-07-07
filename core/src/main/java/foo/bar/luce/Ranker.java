package foo.bar.luce;

import foo.bar.luce.model.MultiSearchResultItem;
import foo.bar.luce.model.SearchResultItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Ranker {
    private static final Logger LOG = LoggerFactory.getLogger(Ranker.class);

    /**
     * Rank search result tokens by distance.
     *
     * @param query                 exact query string entered by user
     * @param multiSearchResultItem independent token positions from finder.
     * @return search result
     */
    public SearchResultItem rank(String query, MultiSearchResultItem multiSearchResultItem) {
        int queryLength = query.length();

        Map<Integer, String> terms = multiSearchResultItem.getTerms();
        List<Integer> indexTokenPositions = new ArrayList<>(terms.keySet());
        ArrayList<Integer> resultPositions = new ArrayList<>();


        for (int i = 0; i < indexTokenPositions.size() - queryLength; i++) {
            List<Integer> frame = indexTokenPositions.subList(i, i + queryLength);

            Integer indexAtFrameStart = frame.get(0);
            Integer indexAtFrameEnd = frame.get(queryLength - 1);

            //check if tokens in the frame are sequential
            if (indexAtFrameEnd - indexAtFrameStart + 1 != queryLength) {
                continue;
            }

            StringBuilder b = new StringBuilder();
            for (Integer p : frame) {
                b.append(terms.get(p));
            }
            String frameString = b.toString();


            if (frameString.equalsIgnoreCase(query)) {
                resultPositions.add(indexAtFrameStart);
            }

        }
        return new SearchResultItem(multiSearchResultItem.getFilename(), query, resultPositions);
    }


    /**
     * Levenshtein distance.
     *
     * @param left  string
     * @param right string
     * @return int
     */
    public int distance(String left, String right) {
        int lenL = left.length();
        int lenR = right.length();

        int[] p = new int[lenL + 1];
        int i, j, up, upL, cost;
        char rightJ;

        for (i = 0; i <= lenL; i++) {
            p[i] = i;
        }

        for (j = 1; j <= lenR; j++) {
            upL = p[0];
            rightJ = right.charAt(j - 1);   //todo: distance will be off for 1 by each surrogate key
            p[0] = j;

            for (i = 1; i <= lenL; i++) {
                up = p[i];
                cost = left.charAt(i - 1) == rightJ ? 0 : 1;
                p[i] = Math.min(
                        Math.min(
                                p[i - 1] + 1,
                                p[i] + 1),
                        upL + cost);
                upL = up;
            }
        }
        return p[lenL];
    }
}
