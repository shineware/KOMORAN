package kr.co.shineware.nlp.komoran.test;

import java.util.List;

import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.util.common.model.Pair;

public class KomoranTest {

	public static void main(String[] args) throws Exception {
		Komoran komoran = new Komoran("models");
		komoran.setFWDic("user_data/fwd.user");
		komoran.setUserDic("user_data/dic.user");
		List<Pair<String,String>> analyzeReulstList = komoran.analyze("벌여도");
		for (Pair<String, String> token : analyzeReulstList) {
			System.out.println(token);
		}
	}
}
