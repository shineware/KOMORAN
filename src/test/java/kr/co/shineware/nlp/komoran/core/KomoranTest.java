package kr.co.shineware.nlp.komoran.core;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by shin285 on 2016. 5. 12..
 */
public class KomoranTest {
	private Komoran komoran;
	@Before
	public void init() throws Exception {
		this.komoran = new Komoran("models");
	}
	@Test
	public void analyze() throws Exception {
		this.komoran.analyze("감사합니다! 바람과 함께 사라지다는 진짜 재밌었어요! nice good!");
	}

	@Test
	public void load() throws Exception {
		this.komoran.load("models");
	}

	@Test
	public void setFWDic() throws Exception {
		this.komoran.setFWDic("user_data/fwd.user");
		this.komoran.analyze("감사합니다! 바람과 함께 사라지다는 진짜 재밌었어요! nice good!");
	}

	@Test
	public void setUserDic() throws Exception {
		this.komoran.setUserDic("user_data/dic.user");
		this.komoran.analyze("감사합니다! 바람과 함께 사라지다는 진짜 재밌었어요! nice good!");
	}

}