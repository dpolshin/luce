package foo.bar.luce;

import foo.bar.luce.index.Analyzer;
import foo.bar.luce.index.WordTokenizer;
import foo.bar.luce.model.*;
import foo.bar.luce.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Finder {
    private static final Logger LOG = LoggerFactory.getLogger(Finder.class);

    public enum Mode {All, Exact}

    private IndexRegistry indexRegistry;
    private FileRegistry fileRegistry;


    public Finder(IndexRegistry indexRegistry, FileRegistry fileRegistry) {
        this.indexRegistry = indexRegistry;
        this.fileRegistry = fileRegistry;
    }


    public List<SearchResultItem> find(String query, Mode mode) {
        WordTokenizer tokenizer = new WordTokenizer(query);
        Stream<Token> rawTokenString = tokenizer.stream();
        Analyzer analyzer = new Analyzer(rawTokenString);
        Stream<Token> tokenStream = analyzer.analyze();

        LOG.info("search terms: ");
        List<Token> queryTerms = tokenStream
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
            //merge search results for same file
            Map<String, List<Position>> searchResult = new HashMap<>();
            for (Token term : queryTerms) {
                List<SearchResultItem> items = singleTermSearch(term.getToken());
                for (SearchResultItem item : items) {
                    if (searchResult.containsKey(item.getFilename())) {
                        searchResult.get(item.getFilename()).addAll(item.getPositions());
                    } else {
                        searchResult.put(item.getFilename(), item.getPositions());
                    }
                }
            }

            List<SearchResultItem> resultItems = new LinkedList<>();
            searchResult.forEach((s, positions) -> resultItems.add(new SearchResultItem(s, "", positions)));

            return resultItems;
        } else if (mode.equals(Mode.Exact)) {
            ArrayList<SearchResultItem> result = new ArrayList<>();

            List<MultiSearchResultItem> multiSearchResultItems = multipleTermSearch(query, queryTerms);
            Ranker ranker = new Ranker();

            //todo: add tf-idf ranking over files;
            for (MultiSearchResultItem item : multiSearchResultItems) {
                SearchResultItem ranked = ranker.rank(query, queryTerms, item);
                result.add(ranked);
            }

            return result;
        }

        return Collections.emptyList();
    }


    private List<SearchResultItem> singleTermSearch(String term) {
        //fast cache search
        Map<FileDescriptor, IndexSegment> indexCache = indexRegistry.getIndexCache();
        Set<FileDescriptor> keySet = indexCache.keySet();

        List<SearchResultItem> searchResult = keySet.parallelStream()
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
                indexRegistry.cacheSegment(file, indexSegment);
            } else {
                LOG.info("index segment file not found for {}", file.getLocation());
                continue;
            }

            if (indexSegment.getSegment().containsKey(term)) {
                searchResult.add(new SearchResultItem(file.getLocation(), term, indexSegment.getSegment().get(term)));
            }
        }
        return searchResult;
    }


    //used for exact mode of search
    private List<MultiSearchResultItem> multipleTermSearch(String rawTerm, List<Token> terms) {

        //fast cache search
        Map<FileDescriptor, IndexSegment> indexCache = indexRegistry.getIndexCache();
        Set<FileDescriptor> indexCacheKeys = indexCache.keySet();

        Predicate<FileDescriptor> containsAllTerms = key -> terms.stream().allMatch(t -> indexCache.get(key).getSegment().containsKey(t.getToken()));
        BinaryOperator<String> mergeFunction = (v1, v2) -> v2;
        Collector<Pair<Integer, String>, ?, TreeMap<Integer, String>> treeMapCollector = Collectors.toMap(Pair::getLeft, Pair::getRight, mergeFunction, TreeMap::new);

        //for each search descriptor get result
        Function<FileDescriptor, MultiSearchResultItem> toMultiSearchResultMapper = descriptor -> {

            //for each term get positions
            Map<Integer, String> collect = terms.stream()
                    //for each position map it to token
                    .flatMap(term -> indexCache.get(descriptor).getSegment().get(term.getToken()).stream()
                            .map(position -> new Pair<>(position.getStart(), term.getToken()))
                    )
                    .collect(treeMapCollector);

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
                indexRegistry.cacheSegment(file, indexSegment);
            } else {
                LOG.info("index segment file not found for {}", file.getLocation());
                continue;
            }

            boolean contains = terms.stream().allMatch(term -> indexSegment.getSegment().containsKey(term.getToken()));

            if (contains) {
                Map<Integer, String> collect = terms.stream()
                        .flatMap(term -> indexSegment.getSegment().get(term.getToken()).stream()
                                .map(position -> new Pair<>(position.getStart(), term.getToken()))
                        )
                        .collect(treeMapCollector);
                MultiSearchResultItem item = new MultiSearchResultItem(file.getLocation(), collect);
                searchResult.add(item);
            }

        }
        return searchResult;
    }
}
