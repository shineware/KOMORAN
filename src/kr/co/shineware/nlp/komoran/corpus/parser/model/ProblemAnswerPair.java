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
package kr.co.shineware.nlp.komoran.corpus.parser.model;

import java.util.List;

import kr.co.shineware.nlp.komoran.corpus.parser.CorpusParser;
import kr.co.shineware.util.common.model.Pair;

/**
 * {@link CorpusParser}에 사용되는 model형태의 클래스로써 get,set 메소드만 존재함 <br>
 * problem은 원본 데이터를 지칭하며, answer는 원본 데이터의 분석된 결과(태깅된 결과)를 지칭함
 * @author Junsoo Shin <jsshin@shineware.co.kr>
 * @version 2.1
 * @since 2.1
 */
public class ProblemAnswerPair {
	private String problem,answer;
	private List<Pair<String,String>> answerList;

	public String getProblem() {
		return problem;
	}

	public void setProblem(String problem) {
		this.problem = problem;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public List<Pair<String,String>> getAnswerList() {
		return answerList;
	}

	public void setAnswerList(List<Pair<String,String>> answerList) {
		this.answerList = answerList;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProblemAnswerPair [problem=");
		builder.append(problem);
		builder.append(", answer=");
		builder.append(answer);
		builder.append(", answerList=");
		builder.append(answerList);
		builder.append("]");
		return builder.toString();
	}
	
}
