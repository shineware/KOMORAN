package kr.co.shineware.nlp.komoran.util;

import kr.co.shineware.nlp.komoran.core.model.Parser;

public class ParserRunnable implements Runnable {

    private final Parser parser;

    public ParserRunnable(Parser parser){
        this.parser = parser;
    }

    @Override
    public void run() {
        parser.parse();
    }
}
