package foo.bar.luce.index;

import foo.bar.luce.model.Token;

import java.util.stream.Stream;

public interface Tokenizer {

    Stream<Token> stream();
}
