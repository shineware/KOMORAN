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
package kr.co.shineware.nlp.komoran.test;

import java.nio.file.Paths;
import java.util.List;

import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;

public class KomoranTest {

	public static void main(String[] args) throws Exception {

		System.out.println(Paths.get(".").toAbsolutePath().normalize().toString()
		);
		Komoran komoran = new Komoran("models");
		komoran.setFWDic("user_data/fwd.user");
		komoran.setUserDic("user_data/dic.user");

		String input = "바람과 함께 사라졌어를 봤어!!";
		KomoranResult analyzeResultList = komoran.analyze(input);
		List<Token> tokenList = analyzeResultList.getTokenList();
		for (Token token : tokenList) {
			System.out.println(token);
			System.out.println(input.substring(token.getBeginIndex(), token.getEndIndex()));
		}
		System.out.println(analyzeResultList.getNouns());
		System.out.println(analyzeResultList.getPlainText());
		System.out.println(analyzeResultList.getTokenList());
	}
}
