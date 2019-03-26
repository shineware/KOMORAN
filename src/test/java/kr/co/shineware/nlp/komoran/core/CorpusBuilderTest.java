package kr.co.shineware.nlp.komoran.core;

import kr.co.shineware.nlp.komoran.corpus.builder.CorpusBuilder;
import org.junit.Test;

public class CorpusBuilderTest {
    @Test
    public void buildCorpus() {
        CorpusBuilder corpusBuilder = new CorpusBuilder();

//        corpusBuilder.setExclusiveIrrRule("resources/irrDic.remove.txt");

        corpusBuilder.appendUserDicPath("corpus_builder_example_data/userdic/cities", "txt");
        corpusBuilder.appendUserDic("corpus_builder_example_data/userdic/president.txt");

        corpusBuilder.build("corpus_builder_example_data/corpus/tagged_corpus.txt");

        corpusBuilder.save("user_model");
    }
}
