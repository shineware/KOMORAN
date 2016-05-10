package kr.co.shineware.nlp.komoran.parser;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by shin285 on 2016. 5. 11..
 */
public class KoreanUnitParserTest {
	@Test
	public void parse() throws Exception {
		KoreanUnitParser koreanUnitParser = new KoreanUnitParser();
		String s = koreanUnitParser.parse("감기");
		assertEquals("ㄱㅏㅁㄱㅣ",s);
	}

	@Test
	public void getSyllableAreaList() throws Exception {

	}

	@Test
	public void combine() throws Exception {

	}

}