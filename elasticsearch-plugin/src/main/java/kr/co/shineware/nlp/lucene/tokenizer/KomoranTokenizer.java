package kr.co.shineware.nlp.lucene.tokenizer;

import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.Token;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;

public class KomoranTokenizer extends Tokenizer {

    private Komoran komoran;
    private String buffer;
    private Iterator<Token> tokenIterator;

    private final CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);
    private final OffsetAttribute offsetAttribute = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);
    private int lastOffset;

    public KomoranTokenizer(Komoran komoran) {
        this.komoran = komoran;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        clearAttributes();
        setupInputBuffer();
        analyze();
        return consumeResult();
    }

    private boolean consumeResult() {
        if (tokenIterator.hasNext()) {
            Token token = tokenIterator.next();
            this.charTermAttribute.setEmpty().append(token.getMorph());
            this.positionIncrementAttribute.setPositionIncrement(1);
            this.offsetAttribute.setOffset(
                    correctOffset(token.getBeginIndex()), correctOffset(token.getEndIndex())
            );
            this.typeAttribute.setType(token.getPos());
            return true;
        }
        lastOffset = buffer.length();
        tokenIterator = null;
        buffer = null;
        return false;
    }

    private void analyze() {
        if (tokenIterator == null) {
            tokenIterator = komoran.analyze(buffer).getTokenList().iterator();
        }
    }

    private void setupInputBuffer() throws IOException {
        if (tokenIterator == null) {
            BufferedReader br = new BufferedReader(input);
            buffer = br.readLine();
            br.close();
        }
        if(buffer == null){
            buffer = "";
        }
    }

    @Override
    public void end() throws IOException {
        super.end();
        offsetAttribute.setOffset(correctOffset(lastOffset), correctOffset(lastOffset));
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        tokenIterator = null;
        buffer = null;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }
}