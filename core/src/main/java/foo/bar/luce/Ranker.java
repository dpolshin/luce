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
     * Match search query tokens within result positions.
     *
     * @param query                 exact query string entered by user
     * @param multiSearchResultItem independent token positions from finder.
     * @return search result
     */
    public SearchResultItem matchResult(String query, MultiSearchResultItem multiSearchResultItem) {
        Map<Integer, String> terms = multiSearchResultItem.getTerms();
        List<Integer> indexTokenPositions = new ArrayList<>(terms.keySet());
        List<Integer> resultPositions = matchQuery(query, terms, indexTokenPositions);
        return new SearchResultItem(multiSearchResultItem.getFilename(), query, resultPositions);
    }

    public SearchResultItem matchResult(List<String> query, MultiSearchResultItem multiSearchResultItem) {
        List<Integer> resultPositions = new ArrayList<>();
        Map<Integer, String> terms = multiSearchResultItem.getTerms();
        List<Integer> indexTokenPositions = new ArrayList<>(terms.keySet());

        for (String q : query) {
            List<Integer> subResultPositions = matchQuery(q, terms, indexTokenPositions);
            if (subResultPositions.size() != 0) {
                resultPositions.addAll(subResultPositions);
            }
        }

        return new SearchResultItem(multiSearchResultItem.getFilename(), query.get(0), resultPositions); //todo: highlighter needs all query tokens;
    }

    private List<Integer> matchQuery(String query, Map<Integer, String> terms, List<Integer> indexTokenPositions) {
        List<Integer> resultPositions = new ArrayList<>();
        int queryLength = query.length();


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
        return resultPositions;
    }
}
