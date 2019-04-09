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
package kr.co.shineware.nlp.komoran.corpus.parser;

import java.util.ArrayList;
import java.util.List;

import kr.co.shineware.nlp.komoran.corpus.builder.CorpusBuilder;
import kr.co.shineware.nlp.komoran.corpus.parser.model.ProblemAnswerPair;
import kr.co.shineware.nlp.komoran.exception.FileFormatException;
import kr.co.shineware.util.common.model.Pair;


/**
 * {@link CorpusBuilder}에 사용되는 파서로써 아래와 같은 형태로 이루어져 있는 경우에 사용 가능함<br>
 * 감기는 감기/NNG 는/JKG <br>
 * 자세한 내용은 아래 링크 참조
 * <a href="http://www.shineware.co.kr"> www.shineware.co.kr > FAQ > CorpusBuiler </a>
 * @author Junsoo Shin <jsshin@shineware.co.kr>
 * @version 2.1
 * @since 2.1
 */
public class CorpusParser{

	private static final String PROBLEM_ANSWER_SPLITER = "\t";
	private static final String ANSWER_SPLITER = " ";
	private static final String WORD_POS_SPLITER = "\\/";
	private static final int CONTENTS_COUNT = 2;
	
	/**
	 * 입력된 라인을 파싱하여 {@link ProblemAnswerPair} 형태로 반환
	 * @param line
	 * @return
	 * @throws FileFormatException
	 */
	public ProblemAnswerPair parse(String line) throws FileFormatException {
		String[] problemAnswer = line.split(PROBLEM_ANSWER_SPLITER);
		if(problemAnswer.length != CONTENTS_COUNT){
			throw new FileFormatException("Corpus Format Error. "+line);
		}
		String problem = problemAnswer[0];
		String answer = problemAnswer[1];
		
		List<Pair<String,String>> answerList = new ArrayList<Pair<String,String>>();
		
		
		this.parseAnswer(answer,answerList);
		ProblemAnswerPair paPair = new ProblemAnswerPair();
		paPair.setProblem(problem);
		paPair.setAnswer(answer);
		paPair.setAnswerList(answerList);
		
		return paPair;
	}

	/**
	 * 입력된 정답(태깅된 결과)를 파싱하여 answerList에 저장
	 * @param answer
	 * @param answerList
	 * @throws FileFormatException
	 */
	private void parseAnswer(String answer,List<Pair<String,String>> answerList) throws FileFormatException {
		String[] tmp = answer.trim().split(ANSWER_SPLITER);
		
		String prevWord = "";
		for(int i=0;i<tmp.length;i++){
			
			String token = tmp[i];
			String pos,word;
			String[] wordPos = token.split(WORD_POS_SPLITER);
			
			if(wordPos.length == 2){
				word = wordPos[0];
				pos = wordPos[1];
			}else if(wordPos.length > 2){
				pos = wordPos[wordPos.length-1];
				word = token.substring(0, token.length()-pos.length()-1);
			}else if(wordPos.length == 1){
				prevWord += token+" ";
				continue;
			}else{
				word = "";
				pos = "";
			}
			
			if(word.trim().length() == 0 || pos.trim().length() == 0){
				throw new FileFormatException("Corpus Format Error. "+answer);
			}else{
				word = prevWord + word;
				answerList.add(new Pair<String, String>(word, pos.toUpperCase()));
			}
			prevWord = "";
		}
	}
}
