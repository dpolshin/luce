package foo.bar.luce;

import foo.bar.luce.index.Analyzer;
import foo.bar.luce.index.ToLowerCaseCharFilter;
import foo.bar.luce.index.WordTokenizer;
import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.IndexSegment;
import foo.bar.luce.model.SearchResultItem;
import foo.bar.luce.model.Token;
import foo.bar.luce.util.CharReaderSpliterator;
import foo.bar.luce.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
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
        } else {

            List<String> queryWords = new WordTokenizer(query).stream().map(Token<String>::getToken).collect(Collectors.toList());
            Function<? super TokenPool<Character>, SearchResultItem> ranker = null;

            if (mode.equals(Mode.All) && queryWords.size() > 1) {
                ranker = pool -> new Ranker().matchResult(queryWords, pool.getFileDescriptor().getLocation(), pool.stream());

            } else if (mode.equals(Mode.Exact) || queryWords.size() == 1 || queryTerms.size() == 1) {
                ranker = pool -> new Ranker().matchResult(query, pool.getFileDescriptor().getLocation(), pool.stream());
            }

            resultStream = multipleTermSearch(queryTerms)
                    .map(ranker)
                    .filter(r -> !r.getPositions().isEmpty());
        }
        return resultStream.limit(Constants.MAX_SEARCH_RESULT_SIZE);
    }


    private Stream<TokenPool<Character>> multipleTermSearch(List<Token<Character>> terms) {
        List<Character> distinctTerms = terms.stream()
                .map(Token::getToken)
                .distinct().collect(Collectors.toList());

        //fast cache search
        Map<FileDescriptor, IndexSegment> indexCache = indexRegistry.getIndexCache();
        Set<FileDescriptor> indexCacheKeys = indexCache.keySet();

//        Stream<MultiSearchResultItem> cachedResultStream = indexCacheKeys.parallelStream()
//                .filter(descriptor -> containsAllTerms.test(indexCache.get(descriptor), terms))
//                .map(descriptor -> toMultiSearchResultMapper.apply(descriptor, indexCache.get(descriptor), terms))
//                .sequential();


        //load and search persisted files
        Set<FileDescriptor> indexedFiles = fileRegistry.getIndexedFilDescriptors();
        Set<FileDescriptor> cachedFiles = indexCache.keySet();

        Stream<TokenPool<Character>> tokenPoolStream = indexedFiles.stream()
                .filter(fileDescriptor -> !cachedFiles.contains(fileDescriptor))
                .flatMap(fileDescriptor -> {
                    LOG.info("loading index segment for {}", fileDescriptor.getLocation());
                    return indexRegistry.getIndexSegments(fileDescriptor)
                            .map(chunk -> new Pair<>(fileDescriptor, chunk));
                })

                .filter(pair -> pair.getRight() != null)
                .filter(pair -> containsAllTerms.test(pair.getRight(), distinctTerms))

                .map(pair -> {
                    TokenPool<Character> tokenPool = new TokenPool<>(pair.getLeft());
                    distinctTerms.forEach(term ->
                            tokenPool.addSwimlane(
                                    pair.getRight()
                                            .getSegment()
                                            .get(term)
                                            .stream()
                                            .map(position -> new Token<>(term, position))));
                    return tokenPool;
                });


        //return Stream.concat(cachedResultStream, persistentResultStream);
        return tokenPoolStream;
    }

    //given index segment contains all search terms
    private BiPredicate<IndexSegment, List<Character>> containsAllTerms = (segment, queryTerms)
            -> queryTerms.stream().allMatch(t -> segment.getSegment().containsKey(t));

}
