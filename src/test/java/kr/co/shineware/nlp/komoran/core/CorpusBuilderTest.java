package kr.co.shineware.nlp.komoran.core;

import kr.co.shineware.nlp.komoran.corpus.builder.CorpusBuilder;
import org.junit.Test;

public class CorpusBuilderTest {
    @Test
    public void buildCorpus() {
        CorpusBuilder corpusBuilder = new CorpusBuilder();

        corpusBuilder.appendUserDicPath("corpus_builder_example_data/userdic/cities", "txt");
        corpusBuilder.appendUserDic("corpus_builder_example_data/userdic/president.txt");

        corpusBuilder.build("corpus_builder_example_data/tagged_corpus.txt");
        corpusBuilder.buildPath("corpus_builder_example_data/");
        corpusBuilder.buildPath("corpus_builder_example_data/", "txt");
    }
}
