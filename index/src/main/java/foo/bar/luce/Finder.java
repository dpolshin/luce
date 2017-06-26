package foo.bar.luce;

import foo.bar.luce.index.Analyzer;
import foo.bar.luce.index.WordTokenizer;
import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.IndexSegment;
import foo.bar.luce.model.SearchResultItem;
import foo.bar.luce.model.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Finder {
    private static final Logger LOG = LoggerFactory.getLogger(Finder.class);

    private IndexRegistry indexRegistry;
    private FileRegistry fileRegistry;


    public Finder(IndexRegistry indexRegistry, FileRegistry fileRegistry) {
        this.indexRegistry = indexRegistry;
        this.fileRegistry = fileRegistry;
    }


    public List<SearchResultItem> find(String query) {
        WordTokenizer tokenizer = new WordTokenizer(query);
        Stream<Token> rawTokenString = tokenizer.stream();
        Analyzer analyzer = new Analyzer(rawTokenString);
        Stream<Token> tokenStream = analyzer.analyze();

        LOG.info("search terms: ");
        List<Token> queryTerms = tokenStream
                .peek(t -> LOG.info(t.toString()))
                .collect(Collectors.toList());

        if (queryTerms.size() != 0) {
            String term = queryTerms.get(0).getToken();
            return singleTermSearch(term);
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
}
