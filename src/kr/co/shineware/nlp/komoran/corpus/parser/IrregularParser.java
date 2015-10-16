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

import kr.co.shineware.nlp.komoran.corpus.parser.model.ProblemAnswerPair;
import kr.co.shineware.nlp.komoran.exception.FileFormatException;
import kr.co.shineware.util.common.model.Pair;
import kr.co.shineware.util.common.string.StringUtil;

/**
 * 불규칙 어휘 분석을 위한 parser <br>
 * 교착어 전용
 * @author Junsoo Shin
 * @version 2.1
 * @since 2.1
 *
 */
public class IrregularParser {

	private int answerBeginIndex  = -1;
	private int problemBeginIndex  = -1;
	private int answerEndIndex = -1;
	private int problemEndIndex = -1;

	/**
	 * 입력된 problem(어절)과 answerList(형태소, 품사)를 비교하여 변화가 일어나는 구간을 pair형태로 반환 <br>
	 * problem과 answerList의 형태소는 자소 단위로 구성되어 있어야함
	 * @param problem
	 * @param answerList
	 * @return
	 */
	public List<Pair<String, String>> parse(String problem, List<Pair<String,String>> answerList){
		this.setBeginIdx(problem,answerList);
		this.setEndIdx(problem,answerList);
		
		return getIrregularRules(problem,answerList);
	}
	private Pair<String,String> expandIrregularRule(String problem,List<Pair<String,String>> answerList,int problemBeginIndex,int problemEndIndex,int answerBeginIndex,int answerEndIndex)
	{
		StringBuffer centerConvertRule = new StringBuffer();
		for(int i=answerBeginIndex;i<answerEndIndex+1;i++){			
			centerConvertRule.append(answerList.get(i).getFirst());
			centerConvertRule.append("/");
			centerConvertRule.append(answerList.get(i).getSecond());
			centerConvertRule.append(" ");
		}

		String prevConvertRule = "";
		String nextConvertRule = "";
		String convertRule = "";
		int tmpProblemBeginIndex = problemBeginIndex;
		int tmpProblemEndIndex = problemEndIndex;
		if(answerBeginIndex != 0){
			prevConvertRule += answerList.get(answerBeginIndex-1).getFirst();
			prevConvertRule += "/";
			prevConvertRule += answerList.get(answerBeginIndex-1).getSecond();
			prevConvertRule += " ";
			tmpProblemBeginIndex = problemBeginIndex - answerList.get(answerBeginIndex-1).getFirst().length();
		}

		if(answerEndIndex+1 != answerList.size()){
			nextConvertRule += answerList.get(answerEndIndex+1).getFirst();
			nextConvertRule += "/";
			nextConvertRule += answerList.get(answerEndIndex+1).getSecond();
			nextConvertRule += " ";
			tmpProblemEndIndex = problemEndIndex + answerList.get(answerEndIndex+1).getFirst().length();
		}
		convertRule = prevConvertRule + centerConvertRule.toString() + nextConvertRule.trim();
		String irrProblem = problem.substring(tmpProblemBeginIndex, tmpProblemEndIndex);
		return new Pair<String, String>(irrProblem, convertRule.trim());	
	}
	
	
	private Pair<String,String> expandIrregularRule(String problem,List<Pair<String,String>> answerList){
		return this.expandIrregularRule(problem, answerList, this.problemBeginIndex, this.problemEndIndex,this.answerBeginIndex, this.answerEndIndex);
	}

	/**
	 * 입력된 problem과 answerList로부터 불규칙 rule 생성 <br>
	 * 단, 불규칙이 발생하는 시작지점과 끝지점에 대한 인덱스가 미리 계산되어 있어야함
	 * @param problem
	 * @param answerList
	 * @return
	 */
	private List<Pair<String, String>> getIrregularRules(String problem,List<Pair<String,String>> answerList){
		List<Pair<String,String>> irrRuleList = new ArrayList<>();
		try{
			if(!this.isCleanRule(problem,answerList)){
				return irrRuleList;
			}
			
			//불규칙 시작지점과 끝지점이 cross 되는 경우
			//알리가 -> 알+ㄹ+리가
			//시작지점 = 4 ( ㅣㄱㅏ )
			//끝 지점 = 2 ( ㅇㅏ)
			if(problemBeginIndex > problemEndIndex){

				//시작 지점 처리
				String irrProblem = problem.substring(0,problemEndIndex);
				//시작 지점의 problem 길이가 0인거나 불규칙 패턴의 형태소가 하나인 경우
				if(irrProblem.trim().length() == 0 || answerEndIndex == 0){
					irrRuleList.add(this.expandIrregularRule(problem,answerList,0,problemEndIndex,0,answerEndIndex));
				}else{
					this.appendIrrRule(irrRuleList,irrProblem,answerList,0,answerEndIndex);					
				}

				//끝 지점 처리
				irrProblem = problem.substring(problemBeginIndex);
				//끝 지점의 problem 길이가 0인거나 불규칙 패턴의 형태소가 하나인 경우
				if(irrProblem.trim().length() == 0 || answerBeginIndex == answerList.size()-1){
					irrRuleList.add(this.expandIrregularRule(problem,answerList,problemBeginIndex,problem.length(),answerBeginIndex,answerList.size()-1));
				}else{
					this.appendIrrRule(irrRuleList,irrProblem,answerList,answerBeginIndex,answerList.size()-1);
				}
				irrProblem = null;
			}
			//축약인 경우
			//너라고 -> 너+이+라고
			//불규칙 패턴의 형태소를 앞,뒤로 하나씩 확장
			//"null -> 이" 의 패턴을 "너라고 -> 너+이+라고"로 확장 
			else if(problemBeginIndex == problemEndIndex){
				irrRuleList.add(this.expandIrregularRule(problem,answerList));
			}
			else{			
				
				String irrProblem = problem.substring(problemBeginIndex,problemEndIndex);
				
				//탈락인 경우
				//어떠냐 -> 어떻+냐
				//어떠 -> null 인 경우 단일 형태소 그대로 사용 (어떠 -> 어떻) 
				if(answerBeginIndex == answerEndIndex){
					int extractLength = problemEndIndex - problemBeginIndex;
					if(extractLength < 4){
						irrRuleList.add(this.expandIrregularRule(problem,answerList));
					}
					else{
						this.appendIrrRule(irrRuleList,irrProblem,answerList,answerBeginIndex,answerEndIndex);
					}
				}else{
					this.appendIrrRule(irrRuleList,irrProblem,answerList,answerBeginIndex,answerEndIndex);
				}
				irrProblem = null;
			}
		}catch(Exception e){
//			e.printStackTrace();
		}
		return irrRuleList;
	}

	/**
	 * 불규칙이 멀쩡한 규칙인지 확인
	 * @param problem
	 * @param answerList
	 * @return
	 */
	private boolean isCleanRule(String problem,
			List<Pair<String, String>> answerList) {
		
		if(answerList.get(answerEndIndex).getSecond().equals("VCP")){
			return false;
		}
		
		for(int i=answerBeginIndex;i<=answerEndIndex;i++){
			if(answerList.get(i).getSecond().contains("NNP") || answerList.get(i).getSecond().contains("NNG") || answerList.get(i).getSecond().contains("JK"))
			{
				return false;
			}
		}
		for(int i=0;i<problem.length();i++){
			if(!StringUtil.isKorean(problem.charAt(i))){
				return false;
			}
		}
		
		return true;
	}
	private void appendIrrRule(List<Pair<String, String>> irrRuleList,
			String problem, List<Pair<String, String>> answerList, int answerBeginIndex,
			int answerEndIndex) {
		StringBuffer convertRule = new StringBuffer();
		for(int i=answerBeginIndex;i<answerEndIndex+1;i++){
			convertRule.append(answerList.get(i).getFirst());
			convertRule.append("/");
			convertRule.append(answerList.get(i).getSecond());
			convertRule.append(" ");
		}
		irrRuleList.add(new Pair<String, String>(problem, convertRule.toString().trim()));
		convertRule = null;
	}

	public List<Pair<String, String>> parse(String line){
		CorpusParser parser = new CorpusParser();
		try {
			ProblemAnswerPair pair = parser.parse(line);
			if(this.isIrregular(pair)){
				return this.parse(pair.getProblem(), pair.getAnswerList());
			}
		} catch (FileFormatException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Pair<String,String>> parse(ProblemAnswerPair pair){
		if(this.isIrregular(pair)){
			return this.parse(pair.getProblem(), pair.getAnswerList());
		}
		return null;
	}

	private boolean isIrregular(ProblemAnswerPair pair) {
		return this.isIrregular(pair.getProblem(), pair.getAnswerList());
	}

	private boolean isIrregular(String problem, List<Pair<String, String>> answerList) {
		StringBuffer answer = new StringBuffer();
		for (Pair<String, String> pair : answerList) {
			answer.append(pair.getFirst());
		}
		return !StringUtil.getKorean(problem).equals(StringUtil.getKorean(answer.toString()));
	}

	private void setEndIdx(String problem, List<Pair<String, String>> answerList) {
		//right to left 탐색
		int ptr = problem.length();
		for(int i=answerList.size()-1;i>=0;i--){
			String answerMorph = answerList.get(i).getFirst();

			if(ptr-answerMorph.length() < 0){
				answerEndIndex = i;
				problemEndIndex = ptr;
				break;
			}
			String problemWord = problem.substring(ptr - answerMorph.length(), ptr);
			if(!answerMorph.equals(problemWord)){
				answerEndIndex = i;
				problemEndIndex = ptr;
				break;
			}
			ptr = ptr - answerMorph.length();
			answerMorph = null;
			problemWord = null;
		}
	}

	private void setBeginIdx(String problem,
			List<Pair<String, String>> answerList) {
		//left to right 탐색
		int ptr = 0;

		for(int i=0;i<answerList.size();i++){
			String answerMorph = answerList.get(i).getFirst();
			//음운 추가 현상인 경우
			//ex : 나가 -> 나가+아, 알 -> 알+ㄹ, 누군지 -> 누구+인지 ('이'추가)
			//누군지 -> 누구+인지
			if(ptr + answerMorph.length() > problem.length()){
				answerBeginIndex = i;
				problemBeginIndex = ptr;
				break;
			}
			String problemWord = problem.substring(ptr, ptr+answerMorph.length());
			//단어가 다르면. 즉, 불규칙 시작 부분이면
			if(!answerMorph.equals(problemWord)){
				answerBeginIndex = i;
				problemBeginIndex = ptr;
				break;
			}
			ptr+=answerMorph.length();
			answerMorph = null;
			problemWord = null;
		}
	}
}
