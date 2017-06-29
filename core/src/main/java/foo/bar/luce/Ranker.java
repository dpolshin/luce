package foo.bar.luce;

import foo.bar.luce.model.MultiSearchResultItem;
import foo.bar.luce.model.Position;
import foo.bar.luce.model.SearchResultItem;
import foo.bar.luce.model.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class Ranker {
    private static final Logger LOG = LoggerFactory.getLogger(Ranker.class);
    private static final int DISTANCE_THRESHOLD_PER_TOKEN = 2;


    /**
     * Rank search result tokens by distance metrics and remove ones that above threshold.
     *
     * @param query                 exact query string entered by user
     * @param queryTerms            query terms by tokenizer
     * @param multiSearchResultItem independent token positions from finder.
     * @return search result
     */
    public SearchResultItem rank(String query, List<Token> queryTerms, MultiSearchResultItem multiSearchResultItem) {
        int queryLength = query.length();
        int querysize = queryTerms.size();
        int maxDistance = DISTANCE_THRESHOLD_PER_TOKEN * querysize;


        Map<Integer, String> terms = multiSearchResultItem.getTerms();
        Iterator<Integer> iterator = terms.keySet().iterator();

        ArrayList<Position> positions = new ArrayList<>();

        frame:
        while (iterator.hasNext()) {
            ArrayList<String> frame = new ArrayList<>();
            int frameStart = 0;
            int frameEnd = 0;


            for (int i = 0; i < querysize; i++) {
                if (iterator.hasNext()) {
                    Integer position = iterator.next();
                    frame.add(terms.get(position));

                    if (i == 0) {
                        frameStart = position;
                    }

                    if (i == querysize - 1) {
                        frameEnd = position + terms.get(position).length();
                    }

                } else {
                    continue frame;
                }
            }

            int positionDistance = Math.abs(frameEnd - frameStart - queryLength);

            int levenshteinDistance = distance(query.toLowerCase(), String.join(" ", frame));
            LOG.trace("probing string: {}, lev distance: {}, index distance: {}", frame, levenshteinDistance, positionDistance);

            if (levenshteinDistance < maxDistance && positionDistance < maxDistance) {
                positions.add(new Position(frameStart, frameEnd));
            }
        }

        return new SearchResultItem(multiSearchResultItem.getFilename(), query, positions);
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
