package foo.bar.luce;

import foo.bar.luce.model.SearchResultItem;
import foo.bar.luce.model.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Ranker {

    /**
     * Match search query tokens within result positions.
     *
     * @param query exact query string entered by user
     * @return search result
     */
    public SearchResultItem matchResult(String query, String filename, Stream<Token<Character>> tokenStream) {
        List<Token<Character>> indexTokenPositions = tokenStream.collect(Collectors.toList());
        List<Token<String>> positions = matchQuery(query, indexTokenPositions);
        return new SearchResultItem(filename, positions);
    }

    /**
     * Match multi word search query tokens within result positions.
     *
     * @param query exact query string entered by user
     * @return search result
     */
    public SearchResultItem matchResult(List<String> query, String filename, Stream<Token<Character>> tokenStream) {
        List<Token<Character>> indexTokenPositions = tokenStream.collect(Collectors.toList());
        List<Token<String>> resultPositions = new ArrayList<>();

        for (String q : query) {
            List<Token<String>> subResultPositions = matchQuery(q, indexTokenPositions);
            if (subResultPositions.size() != 0) {
                resultPositions.addAll(subResultPositions);
            }
        }

        return new SearchResultItem(filename, resultPositions);
    }

    private List<Token<String>> matchQuery(String query, List<Token<Character>> indexTokenPositions) {
        List<Token<String>> resultPositions = new ArrayList<>();
        int queryLength = query.length();


        for (int i = 0; i < indexTokenPositions.size(); i++) {
            if (i + queryLength > indexTokenPositions.size()) {
                break;
            }

            List<Token<Character>> frame = indexTokenPositions.subList(i, i + queryLength);

            Integer indexAtFrameStart = frame.get(0).getPosition();
            Integer indexAtFrameEnd = frame.get(queryLength - 1).getPosition();

            //check if tokens in the frame are sequential
            if (indexAtFrameEnd - indexAtFrameStart + 1 != queryLength) {
                continue;
            }

            StringBuilder b = new StringBuilder();
            for (Token<Character> p : frame) {
                b.append(p.getToken());
            }
            String frameString = b.toString();


            if (frameString.equalsIgnoreCase(query)) {
                resultPositions.add(new Token<>(query, indexAtFrameStart));
            }
        }
        return resultPositions;
    }
}
