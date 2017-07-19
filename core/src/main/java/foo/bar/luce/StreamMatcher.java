package foo.bar.luce;

import foo.bar.luce.model.SearchResultItem;
import foo.bar.luce.model.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class StreamMatcher<T> implements Consumer<Token<T>> {
    private List<Token<T>> frame = new ArrayList<>();
    private List<Token<String>> resultPositions = new ArrayList<>();
    private String query;
    private String fileName;

    public StreamMatcher(String query, String fileName) {
        this.query = query;
        this.fileName = fileName;
    }

    public SearchResultItem getResult() {
        return new SearchResultItem(fileName, resultPositions);
    }


//    public SearchResultItem matchResult(List<String> query, String filename, Stream<Token<Character>> tokenStream) {
//        List<Token<Character>> indexTokenPositions = tokenStream.collect(Collectors.toList());
//        List<Token<String>> resultPositions = new ArrayList<>();
//
//        Map<String, Boolean> termsMatch = new HashMap<>();
//
//        for (String q : query) {
//            List<Token<String>> subResultPositions = matchQuery(q, indexTokenPositions);
//            if (subResultPositions.size() != 0) {
//                resultPositions.addAll(subResultPositions);
//                termsMatch.put(q, Boolean.TRUE);
//            }
//        }
//
//
//        todo: this is important for 'All' mode:
//        if (!termsMatch.keySet().containsAll(query)) {
//            resultPositions = Collections.emptyList();
//        }
//        return new SearchResultItem(filename, resultPositions);
//    }


    @Override
    public void accept(Token<T> token) {
        int queryLength = query.length();

        frame.add(token);

        if (frame.size() > queryLength) {
            frame = frame.subList(1, frame.size());
        }

        if (frame.size() < queryLength) {
            return;
        }

        Integer indexAtFrameStart = frame.get(0).getPosition();
        Integer indexAtFrameEnd = frame.get(queryLength - 1).getPosition();

        //check if tokens in the frame are sequential
        if (indexAtFrameEnd - indexAtFrameStart + 1 != queryLength) {
            return;
        }

        StringBuilder b = new StringBuilder();
        for (Token<T> p : frame) {
            b.append(p.getToken());
        }
        String frameString = b.toString();


        if (frameString.equalsIgnoreCase(query)) {
            resultPositions.add(new Token<>(query, indexAtFrameStart));
        }
    }
}
