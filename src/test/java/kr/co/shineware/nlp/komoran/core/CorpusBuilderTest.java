package kr.co.shineware.nlp.komoran.core;

import kr.co.shineware.nlp.komoran.corpus.builder.CorpusBuilder;
import org.junit.Test;

public class CorpusBuilderTest {
    @Test
    public void buildCorpus() {
        CorpusBuilder corpusBuilder = new CorpusBuilder();

//        corpusBuilder.setExclusiveIrrRule("resources/irrDic.remove.txt");

        corpusBuilder.appendUserDicPath("src/test/resources/userdic/cities", "txt");
        corpusBuilder.appendUserDic("src/test/resources/userdic/president.txt");

        corpusBuilder.build("src/test/resources/training/tagged_corpus.txt");

        corpusBuilder.save("user_model");
    }
}
