package foo.bar.luce;

import foo.bar.luce.index.Analyzer;
import foo.bar.luce.index.ToLowerCaseCharFilter;
import foo.bar.luce.index.WordTokenizer;
import foo.bar.luce.model.*;
import foo.bar.luce.util.CharReaderSpliterator;
import foo.bar.luce.util.Pair;
import foo.bar.luce.util.TriFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class Finder {
    private static final Logger LOG = LoggerFactory.getLogger(Finder.class);

    public enum Mode {All, Exact}

    private IndexRegistry indexRegistry;
    private FileRegistry fileRegistry;
    private Analyzer<Character> analyzer = new Analyzer<>(new ToLowerCaseCharFilter());


    public Finder(IndexRegistry indexRegistry, FileRegistry fileRegistry) {
        this.indexRegistry = indexRegistry;
        this.fileRegistry = fileRegistry;
    }


    public Stream<SearchResultItem> find(String query, Mode mode) {
        LOG.info("search terms: ");
        List<Token<Character>> queryTerms = StreamSupport
                .stream(new CharReaderSpliterator(new StringReader(query)), false)
                .flatMap(analyzer::analyze)
                .peek(t -> LOG.info(t.toString()))
                .collect(Collectors.toList());

        int termsCount = queryTerms.size();
        Stream<SearchResultItem> resultStream;

        if (termsCount == 0) {
            resultStream = Stream.empty();
        } else if (termsCount == 1) {
            resultStream = singleTermSearch(queryTerms.get(0).getToken());
        } else {

            List<String> queryWords = new WordTokenizer(query).stream().map(Token<String>::getToken).collect(Collectors.toList());
            Function<MultiSearchResultItem, SearchResultItem> ranker = null;

            if (mode.equals(Mode.All) && queryWords.size() > 1) {
                ranker = result -> new Ranker().matchResult(queryWords, result);
            } else if (mode.equals(Mode.Exact) || queryWords.size() == 1) {
                ranker = result -> new Ranker().matchResult(query, result);
            }

            resultStream = multipleTermSearch(queryTerms).map(ranker).filter(r -> !r.getPositions().isEmpty());
        }
        return resultStream.limit(Constants.MAX_SEARCH_RESULT_SIZE);
    }


    //fast shorthand search for single-token query
    private Stream<SearchResultItem> singleTermSearch(Character term) {
        //fast cache search
        Map<FileDescriptor, IndexSegment> indexCache = indexRegistry.getIndexCache();
        Set<FileDescriptor> keySet = indexCache.keySet();

        Stream<SearchResultItem> cachedResultStream = keySet.parallelStream()
                .filter(key -> indexCache.get(key).getSegment().containsKey(term))
                .map(key -> new SearchResultItem(key.getLocation(), term.toString(), indexCache.get(key).getSegment().get(term)))
                .sequential();


        //load and search persisted files
        Set<FileDescriptor> indexedFiles = fileRegistry.getIndexedFilDescriptors();
        Set<FileDescriptor> cachedFiles = indexCache.keySet();

        Stream<SearchResultItem> persistentResultStream = indexedFiles.stream()
                .filter(fileDescriptor -> !cachedFiles.contains(fileDescriptor))
                .map(fileDescriptor -> {
                    LOG.info("loading index segment for {}", fileDescriptor.getLocation());
                    return new Pair<>(fileDescriptor.getLocation(), indexRegistry.getIndexSegment(fileDescriptor));
                })
                .filter(pair -> pair.getRight() != null)
                .filter(pair -> pair.getRight().getSegment().containsKey(term))
                .map(pair -> new SearchResultItem(pair.getLeft(), term.toString(), pair.getRight().getSegment().get(term)));


        return Stream.concat(cachedResultStream, persistentResultStream);
    }


    private Stream<MultiSearchResultItem> multipleTermSearch(List<Token<Character>> terms) {

        //fast cache search
        Map<FileDescriptor, IndexSegment> indexCache = indexRegistry.getIndexCache();
        Set<FileDescriptor> indexCacheKeys = indexCache.keySet();

        Stream<MultiSearchResultItem> cachedResultStream = indexCacheKeys.parallelStream()
                .filter(descriptor -> containsAllTerms.test(indexCache.get(descriptor), terms))
                .map(descriptor -> toMultiSearchResultMapper.apply(descriptor, indexCache.get(descriptor), terms))
                .sequential();


        //load and search persisted files
        Set<FileDescriptor> indexedFiles = fileRegistry.getIndexedFilDescriptors();
        Set<FileDescriptor> cachedFiles = indexCache.keySet();

        Stream<MultiSearchResultItem> persistentResultStream = indexedFiles.stream()
                .filter(fileDescriptor -> !cachedFiles.contains(fileDescriptor))
                .map(fileDescriptor -> {
                    LOG.info("loading index segment for {}", fileDescriptor.getLocation());
                    return new Pair<>(fileDescriptor, indexRegistry.getIndexSegment(fileDescriptor));
                })
                .filter(pair -> pair.getRight() != null)
                .filter(pair -> containsAllTerms.test(pair.getRight(), terms))
                .map(pair -> toMultiSearchResultMapper.apply(pair.getLeft(), pair.getRight(), terms));

        return Stream.concat(cachedResultStream, persistentResultStream);
    }


    private BinaryOperator<Character> mergeFunction = (v1, v2) -> v2;
    private Collector<Pair<Integer, Character>, ?, Map<Integer, Character>> mapCollector = Collectors.toMap(Pair::getLeft, Pair::getRight, mergeFunction, TreeMap::new);

    //given index segment contains all search terms
    private BiPredicate<IndexSegment, List<Token<Character>>> containsAllTerms = (segment, queryTerms) -> queryTerms.stream().allMatch(t -> segment.getSegment().containsKey(t.getToken()));

    //for each search descriptor get result
    private TriFunction<FileDescriptor, IndexSegment, List<Token<Character>>, MultiSearchResultItem> toMultiSearchResultMapper = (descriptor, segment, queryTerms) -> {

        //for each term get positions
        Map<Integer, Character> collect = queryTerms.stream()
                //for each position map it to token
                .flatMap(term -> segment.getSegment().get(term.getToken()).stream()
                        .map(position -> new Pair<>(position, term.getToken()))
                )
                .collect(mapCollector);

        return new MultiSearchResultItem(descriptor.getLocation(), collect);
    };

}
