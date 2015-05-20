package kr.co.shineware.nlp.komoran.test;

import kr.co.shineware.nlp.komoran.corpus.builder.CorpusBuilder;

public class CorpusBuilderTest {

	public static void main(String[] args) {
		String trainPathForWindow = "../Datas/pos_tagger/sj2003_convert";
		CorpusBuilder builder = new CorpusBuilder();
		builder.setExclusiveIrrRule("resources/irrDic.remove.txt");
		builder.buildPath(trainPathForWindow,"tag");
//		builder.appendUserDicPath("..\\Datas\\pos_tagger\\user_dic\\", "user");
		builder.save("corpus_build");
	}

}
