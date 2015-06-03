package kr.co.shineware.nlp.komoran.test;

import kr.co.shineware.nlp.komoran.core.Komoran;

public class KomoranTest {

	public static void main(String[] args) {
		Komoran komoran = new Komoran("models");
		komoran.analyze("병이다");
	}

}
