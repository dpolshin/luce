package foo.bar.luce;

import foo.bar.luce.index.Analyzer;
import foo.bar.luce.index.WordTokenizer;
import foo.bar.luce.model.*;
import foo.bar.luce.util.CharReaderSpliterator;
import foo.bar.luce.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class Finder {
    private static final Logger LOG = LoggerFactory.getLogger(Finder.class);

    public enum Mode {All, Exact}

    private IndexRegistry indexRegistry;
    private FileRegistry fileRegistry;
    private Analyzer analyzer = new Analyzer();


    public Finder(IndexRegistry indexRegistry, FileRegistry fileRegistry) {
        this.indexRegistry = indexRegistry;
        this.fileRegistry = fileRegistry;
    }


    public List<SearchResultItem> find(String query, Mode mode) {
        LOG.info("search terms: ");
        List<Token> queryTerms = StreamSupport
                .stream(new CharReaderSpliterator(new StringReader(query)), false)
                .flatMap(analyzer::analyze)
                .peek(t -> LOG.info(t.toString()))
                .collect(Collectors.toList());

        int termsCount = queryTerms.size();

        if (termsCount == 0) {
            return Collections.emptyList();
        }

        if (termsCount == 1) {
            String term = queryTerms.get(0).getToken();
            return singleTermSearch(term);
        }


        if (mode.equals(Mode.All)) {
            List<Token> queryWords = new WordTokenizer(query).stream().collect(Collectors.toList());

            ArrayList<SearchResultItem> result = new ArrayList<>();
            Map<String, List<Integer>> merged = new HashMap<>();

            if (queryWords.size() == 0) {
                return Collections.emptyList();
            }

            for (Token queryWord : queryWords) {


                List<Token> subQueryTerms = StreamSupport
                        .stream(new CharReaderSpliterator(new StringReader(queryWord.getToken())), false)
                        .flatMap(analyzer::analyze).collect(Collectors.toList());

                List<MultiSearchResultItem> multiSearchResultItems = multipleTermSearch(subQueryTerms);
                Ranker ranker = new Ranker();

                for (MultiSearchResultItem item : multiSearchResultItems) {
                    SearchResultItem ranked = ranker.rank(queryWord.getToken(), item);
                    if (ranked.getPositions().size() != 0) {

                        if (merged.containsKey(ranked.getFilename())) {
                            merged.get(ranked.getFilename()).addAll(ranked.getPositions());
                        } else {
                            merged.put(ranked.getFilename(), ranked.getPositions());
                        }
                    }
                }

            }

            for (Map.Entry<String, List<Integer>> e : merged.entrySet()) {
                result.add(new SearchResultItem(e.getKey(), query, e.getValue()));
                if (result.size() == Constants.MAX_SEARCH_RESULT_SIZE) {
                    LOG.info("Search result exceeded max size");
                    return result;
                }
            }

            return result;
        } else if (mode.equals(Mode.Exact)) {
            ArrayList<SearchResultItem> result = new ArrayList<>();

            List<MultiSearchResultItem> multiSearchResultItems = multipleTermSearch(queryTerms);
            Ranker ranker = new Ranker();

            for (MultiSearchResultItem item : multiSearchResultItems) {
                SearchResultItem ranked = ranker.rank(query, item);
                if (ranked.getPositions().size() != 0) {
                    result.add(ranked);
                    if (result.size() == Constants.MAX_SEARCH_RESULT_SIZE) {
                        LOG.info("Search result exceeded max size");
                        return result;
                    }
                }
            }

            return result;
        }

        return Collections.emptyList();
    }


    //fast shorthand search for single-token query
    private List<SearchResultItem> singleTermSearch(String term) {
        //fast cache search
        Map<FileDescriptor, IndexSegment> indexCache = indexRegistry.getIndexCache();
        Set<FileDescriptor> keySet = indexCache.keySet();

        List<SearchResultItem> result = keySet.parallelStream()
                .filter(key -> indexCache.get(key).getSegment().containsKey(term))
                .map(key -> new SearchResultItem(key.getLocation(), term, indexCache.get(key).getSegment().get(term)))
                .sequential().collect(Collectors.toList());


        //load and search persisted files
        Set<FileDescriptor> indexedFiles = fileRegistry.getIndexedFilDescriptors();
        Set<FileDescriptor> cachedFiles = indexCache.keySet();
        Set<FileDescriptor> remaining = indexedFiles.stream().filter(e -> !cachedFiles.contains(e)).collect(Collectors.toSet());

        for (FileDescriptor file : remaining) {
            LOG.info("no file in cache, loading index segment for {}", file.getLocation());
            IndexSegment indexSegment = indexRegistry.getIndexSegment(file);
            if (indexSegment != null) {
                //indexRegistry.cacheSegment(file, indexSegment);
            } else {
                LOG.info("index segment file not found for {}", file.getLocation());
                continue;
            }

            if (indexSegment.getSegment().containsKey(term)) {
                result.add(new SearchResultItem(file.getLocation(), term, indexSegment.getSegment().get(term)));
                if (result.size() == Constants.MAX_SEARCH_RESULT_SIZE) {
                    LOG.info("Search result exceeded max size");
                    return result;
                }
            }
        }
        return result;
    }


    private List<MultiSearchResultItem> multipleTermSearch(List<Token> terms) {

        //fast cache search
        Map<FileDescriptor, IndexSegment> indexCache = indexRegistry.getIndexCache();
        Set<FileDescriptor> indexCacheKeys = indexCache.keySet();

        Predicate<FileDescriptor> containsAllTerms = key -> terms.stream().allMatch(t -> indexCache.get(key).getSegment().containsKey(t.getToken()));
        BinaryOperator<String> mergeFunction = (v1, v2) -> v2;
        Collector<Pair<Integer, String>, ?, Map<Integer, String>> mapCollector = Collectors.toMap(Pair::getLeft, Pair::getRight, mergeFunction, HashMap::new);

        //for each search descriptor get result
        Function<FileDescriptor, MultiSearchResultItem> toMultiSearchResultMapper = descriptor -> {

            //for each term get positions
            Map<Integer, String> collect = terms.stream()
                    //for each position map it to token
                    .flatMap(term -> indexCache.get(descriptor).getSegment().get(term.getToken()).stream()
                            .map(position -> new Pair<>(position, term.getToken()))
                    )
                    .collect(mapCollector);

            return new MultiSearchResultItem(descriptor.getLocation(), collect);
        };

        List<MultiSearchResultItem> searchResult = indexCacheKeys.parallelStream()
                .filter(containsAllTerms)
                .map(toMultiSearchResultMapper)
                .sequential().collect(Collectors.toList());


        //load and search persisted files
        Set<FileDescriptor> indexedFiles = fileRegistry.getIndexedFilDescriptors();
        Set<FileDescriptor> cachedFiles = indexCache.keySet();
        Set<FileDescriptor> remaining = indexedFiles.stream().filter(e -> !cachedFiles.contains(e)).collect(Collectors.toSet());

        for (FileDescriptor file : remaining) {
            LOG.info("no file in cache, loading index segment for {}", file.getLocation());
            IndexSegment indexSegment = indexRegistry.getIndexSegment(file);
            if (indexSegment != null) {
                //indexRegistry.cacheSegment(file, indexSegment);
            } else {
                LOG.info("index segment file not found for {}", file.getLocation());
                continue;
            }

            boolean contains = terms.stream().allMatch(term -> indexSegment.getSegment().containsKey(term.getToken()));

            if (contains) {
                Map<Integer, String> collect = terms.stream()
                        .flatMap(term -> indexSegment.getSegment().get(term.getToken()).stream()
                                .map(position -> new Pair<>(position, term.getToken()))
                        )
                        .collect(mapCollector);//!!!
                MultiSearchResultItem item = new MultiSearchResultItem(file.getLocation(), collect);
                searchResult.add(item);
            }

        }
        return searchResult;
    }
}
