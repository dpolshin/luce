package foo.bar.luce;

import foo.bar.luce.model.FileDescriptor;
import foo.bar.luce.model.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class TokenPool<TT> extends Spliterators.AbstractSpliterator<Token<TT>> {
    private static final Logger LOG = LoggerFactory.getLogger(TokenPool.class);

    private FileDescriptor fileDescriptor;
    private List<LookAheadIterator<Token<TT>>> swimlanes = new ArrayList<>();


    public TokenPool() {
        super(Long.MAX_VALUE, ORDERED | NONNULL);
    }

    public TokenPool(FileDescriptor fileDescriptor) {
        super(Long.MAX_VALUE, ORDERED | NONNULL);
        this.fileDescriptor = fileDescriptor;
    }

    public void addSwimlane(Stream<Token<TT>> swimlane) {
        LookAheadIterator<Token<TT>> lookAheadIterator = new LookAheadIterator<>(swimlane.iterator());
        //LOG.trace("add swimlane, token: {}", lookAheadIterator.peek());
        this.swimlanes.add(lookAheadIterator);
    }

    public FileDescriptor getFileDescriptor() {
        return fileDescriptor;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Token<TT>> action) {
        //noinspection unchecked
        Token<TT>[] frame = new Token[swimlanes.size()];

        if (anyNext(swimlanes)) {
            for (int i = 0; i < swimlanes.size(); i++) {
                frame[i] = swimlanes.get(i).peek();
            }
            int minIndex = minIndex(frame);
            Token<TT> token = frame[minIndex];
            swimlanes.get(minIndex).next();
            action.accept(token);
            return true;
        }

        return false;
    }

    public Stream<Token<TT>> stream() {
        return StreamSupport.stream(this, false);
    }

    private <T> boolean anyNext(List<LookAheadIterator<T>> s) {
        boolean res = false;
        for (LookAheadIterator ss : s) {
            if (ss.hasNext()) {
                res = true;
            }
        }
        return res;
    }

    //get an index within given frame of a token with minimum position
    private int minIndex(Token<TT>[] frame) {
        int index = 0;
        int min = Integer.MAX_VALUE;

        for (int i = 0; i < frame.length; i++) {
            if (frame[i] == null) {
                continue;
            }
            if (frame[i].getPosition() < min) {
                min = frame[i].getPosition();
                index = i;
            }
        }
        return index;
    }


    static class LookAheadIterator<T> {
        private Iterator<T> iterator;
        private T peek;
        private boolean exhausted = false;

        public LookAheadIterator(Iterator<T> iterator) {
            this.iterator = iterator;
        }

        public T peek() {
            try {
                if (peek == null && !exhausted) {
                    peek = iterator.next();
                    return peek;
                } else {
                    return peek;
                }
            } catch (NoSuchElementException e) {
                exhausted = true;
            }
            return null;
        }

        public T next() {
            T next = null;
            if (!exhausted) {
                next = peek;
                peek = null;
            }
            return next;
        }

        public boolean hasNext() {
            peek();
            return !exhausted;
        }
    }
}
