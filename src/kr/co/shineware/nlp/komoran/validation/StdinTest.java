package kr.co.shineware.nlp.komoran.validation;

import kr.co.shineware.nlp.komoran.core.Komoran;

public class StdinTest {

	public static void main(String[] args) {
		Komoran komoran = new Komoran("validation/model_build");
		System.out.println(komoran.analyze("외국관광객이"));
		System.out.println(komoran.analyze("외국관광객이"));
		System.out.println(komoran.analyze("찾아와도"));
	}
}
