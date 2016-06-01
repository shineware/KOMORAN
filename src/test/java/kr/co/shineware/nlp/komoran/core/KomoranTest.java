/*******************************************************************************
 * KOMORAN 3.0 - Korean Morphology Analyzer
 *
 * Copyright 2015 Shineware http://www.shineware.co.kr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package kr.co.shineware.nlp.komoran.core;

import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import kr.co.shineware.util.common.model.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class KomoranTest {
	private Komoran komoran;
	@Before
	public void init() throws Exception {
		this.komoran = new Komoran("models_full");
	}
	@Test
	public void analyze() throws Exception {
		KomoranResult komoranResult = this.komoran.analyze("감사합니다! 바람과 함께 사라지다는 진짜 재밌었어요! nice good!");
		List<Pair<String,String>> pairList = komoranResult.getList();
		for (Pair<String, String> morphPosPair : pairList) {
			System.out.println(morphPosPair);
		}

		List<String> nounList = komoranResult.getNouns();
		for (String noun : nounList) {
			System.out.println(noun);
		}

		System.out.println(komoranResult.getPlainText());

		List<Token> tokenList = komoranResult.getTokenList();
		for (Token token : tokenList) {
			System.out.println(token);
		}
	}

	@Test
	public void load() throws Exception {
		this.komoran.load("models_full");
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