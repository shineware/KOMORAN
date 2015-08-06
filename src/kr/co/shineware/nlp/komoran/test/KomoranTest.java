package kr.co.shineware.nlp.komoran.test;

import java.util.List;

import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.util.common.model.Pair;

public class KomoranTest {

	public static void main(String[] args) throws Exception {
		Komoran komoran = new Komoran("models/");
		List<Pair<String,String>> analyzeReulstList = komoran.analyze("눈이 자꾸 감겼었어.");
		for (Pair<String, String> token : analyzeReulstList) {
			System.out.println(token);
		}
	}
}
