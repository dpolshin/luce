package foo.bar.luce.util;

import foo.bar.luce.model.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Convert Reader into Stream of Tokens.
 */
public class CharReaderSpliterator extends Spliterators.AbstractSpliterator<Token<Character>> implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(CharReaderSpliterator.class);
    protected Reader reader;
    private int position = 0;

    public CharReaderSpliterator() {
        super(Long.MAX_VALUE, ORDERED | NONNULL);
    }

    public CharReaderSpliterator(Reader reader) {
        this();
        this.reader = reader;
    }


    @Override
    public boolean tryAdvance(Consumer<? super Token<Character>> action) {
        try {
            int i;
            //noinspection LoopStatementThatDoesntLoop
            while ((i = reader.read()) > -1) {
                position++;
                Character c = CharUtil.of(i);
                action.accept(new Token<>(c, position - 1));
                return true;
            }
            return false;
        } catch (MalformedInputException | UnmappableCharacterException e) {
            LOG.error("", e);
            throw new UnsupportedContentException(e);
        } catch (IOException e) {
            LOG.error("Exception occurred while reading file", e);
            return false;
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    public Stream<Token<Character>> stream() {
        return StreamSupport.stream(this, false);
    }
}
