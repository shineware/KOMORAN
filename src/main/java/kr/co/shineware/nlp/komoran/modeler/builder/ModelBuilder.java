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
package kr.co.shineware.nlp.komoran.modeler.builder;

import kr.co.shineware.nlp.komoran.constant.FILENAME;
import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.corpus.model.Dictionary;
import kr.co.shineware.nlp.komoran.corpus.model.Grammar;
import kr.co.shineware.nlp.komoran.model.ScoredTag;
import kr.co.shineware.nlp.komoran.modeler.model.*;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;
import kr.co.shineware.util.common.file.FileUtil;
import kr.co.shineware.util.common.model.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

/**
 * 코퍼스 빌드 결과물로 모델 생성</br> 생성되는 데이터는 아래와 같음</br>
 * - 관측 확률(observation.model)</br>
 * - 전이 확률(transition.model)</br>
 * - 불규칙 모델(irregular.model)</br>
 * - 품사 테이블(pos.table)</br>
 * 
 * @author Junsoo Shin <jsshin@shineware.co.kr>
 * @version 2.2
 * @since 2.1
 * 
 */
public class ModelBuilder {

	private Dictionary wordDic;
	private Dictionary irrDic;
	private Grammar grammar;

	private Transition transition;
	private Observation observation;
	private PosTable table;
	private IrregularTrie irrTrie;
	
	private KoreanUnitParser unitParser;
	private String externalDic;

	/**
	 * 입력된 path에 저장된 데이터로 부터 model 빌드 <br>
	 * path 내에는 아래와 같은 데이터들이 포함되어 있어야함 <br>
	 * - 단어 사전 파일 : dic.word
	 * - 불규칙 사전 파일 : dic.irregular
	 * - 품사 간 문법 사전 : grammar.in
	 * @param path 모델을 생성하기 위한 트레이닝 데이터 위치
	 */
	public void buildPath(String path){
		this.unitParser = null;
		this.wordDic = null;
		this.irrDic = null;
		this.grammar = null;

		this.unitParser = new KoreanUnitParser();
		wordDic = new Dictionary(path+File.separator+FILENAME.WORD_DIC);
		this.addExternalDic(this.externalDic);

		irrDic = new Dictionary(path+File.separator+FILENAME.IRREGULAR_DIC);
		grammar = new Grammar(path+File.separator+FILENAME.GRAMMAR);

		Map<String,Integer> totalPrevPOSTf = this.getTotalPrevPOSCount();

		//build POS table
		this.buildPosTable(totalPrevPOSTf);

		//build transition
		this.calTransition(totalPrevPOSTf);

		//build observation
		this.calObservation(totalPrevPOSTf);

		//build irregular dic to trie
		this.buildIrregularDic();
	}



	/**
	 * 불규칙 사전 빌드
	 */
	private void buildIrregularDic() {
		this.irrTrie = new IrregularTrie();
		Set<Entry<String, Map<String,Integer>>> irrDicEntrySet = this.irrDic.getDictionary().entrySet();
		for (Entry<String, Map<String, Integer>> irrConvertTfEntry : irrDicEntrySet) {
			String irr = irrConvertTfEntry.getKey();
			irr = this.unitParser.parse(irr);
			Set<Entry<String,Integer>> convertTfSet = irrConvertTfEntry.getValue().entrySet();
			for (Entry<String, Integer> convertTf : convertTfSet) {
				if(convertTf.getValue() < 2 && convertTfSet.size() > 1)continue;
				IrregularNode irrNode = this.makeIrrNode(irr,this.unitParser.parse(convertTf.getKey()));
				this.irrTrie.put(irr, irrNode);				
			}
		}
	}

	/**
	 * 불규칙 노드 생성 <br>
	 * 불규칙 패턴의 head 품사 및 tail 품사, 불규칙 패턴 내부의 관측/전이 확률 score, 변환 규칙 정보를 포함하고 있음 <br>
	 * @param irr 불규칙 패턴
	 * @param convert 불규칙 패턴에 대해 변환되는 규칙
	 * @return 불규칙 노드를 생성
	 */
	private IrregularNode makeIrrNode(String irr, String convert) {
		
		IrregularNode irrNode = new IrregularNode();
		
		List<Pair<String,Integer>> irrNodeTokens = new ArrayList<>();
		double score = 0;
		StringBuffer morphFormat = new StringBuffer(); 
		String[] tokens = convert.split(" ");
		int prevPosId = -1;
		for(int i=0;i<tokens.length;i++){
			String token = tokens[i];
			int splitIdx = token.lastIndexOf("/");
			String morph = token.substring(0, splitIdx);
			int posId = this.table.getId(token.substring(splitIdx+1));
			irrNodeTokens.add(new Pair<>(morph, posId));
			//set first pos id
			if(i == 0){
				irrNode.setFirstPosId(posId);
			}
			
			//관측 확률
			List<ScoredTag> scoredTagList = this.observation.getTrieDictionary().getValue(morph);
			if(scoredTagList == null){
//				System.err.println(morph+" has no observation score!");
				continue;
			}
			for (ScoredTag scoredTag: scoredTagList) {
				if(scoredTag.getTagId() == posId){
					score += scoredTag.getScore();
					break;
				}
			}
			
			//전이 확률
			if(prevPosId != -1){
//				if(this.transition.get(prevPosId, posId) == null){
//					System.out.println(irr);
//					System.out.println(convert);
//				}
				score += this.transition.get(prevPosId, posId);
			}
			prevPosId = posId;
			
			morphFormat.append(morph);
			//set last morph
			if(i==tokens.length-1){
				irrNode.setLastMorph(morph);
				irrNode.setLastPosId(posId);
				break;
			}
			morphFormat.append("/");
			morphFormat.append(token.substring(splitIdx+1));
			morphFormat.append(" ");
		}

		irrNode.setInnerScore(score);
		irrNode.setTokens(irrNodeTokens);
		irrNode.setMorphFormat(morphFormat.toString());
		return irrNode;
	}	
	/**
	 * 주어진 POS 빈도수 정보를 활용하여 POS table 구축 <br>
	 * POS table은 형태소 분석 단계에서 메모리 사용량 및 속도 향상을 위해 사용됨 <br>
	 * String < Integer
	 * @param totalPrevPOSTf 전체 빈도수
	 */
	private void buildPosTable(Map<String, Integer> totalPrevPOSTf) {
		this.table = new PosTable();
		Set<String> posSet = totalPrevPOSTf.keySet();
		for (String pos : posSet) {
			this.table.put(pos);
		}
		this.table.put(SYMBOL.BOE);
		this.table.put(SYMBOL.EOE);
		this.table.put(SYMBOL.NA);
	}

	/**
	 * WordDictionary의 데이터를 이용하여 관측 확률(observation probability) 계산<br> 
	 * @param totalPrevPOSTf
	 */
	private void calObservation(Map<String, Integer> totalPrevPOSTf) {

		this.observation = new Observation();

		Set<Entry<String, Map<String,Integer>>> wordDicEntrySet = wordDic.getDictionary().entrySet();
		for (Entry<String, Map<String, Integer>> wordPosTfEntry : wordDicEntrySet) {
			String word = wordPosTfEntry.getKey();
			Set<Entry<String,Integer>> posTfSet = wordPosTfEntry.getValue().entrySet();
			for (Entry<String, Integer> posTf : posTfSet) {
				int totalPosTf = totalPrevPOSTf.get(posTf.getKey());
				double observationScore = (double)posTf.getValue()/totalPosTf;
				observationScore = Math.log10(observationScore);
				this.observation.put(word,posTf.getKey(),this.table.getId(posTf.getKey()),observationScore);
			}
		}
	}

	/**
	 * Grammar의 데이터를 이용하여 전이 확률(transition probability) 계산<br>
	 * @param totalPrevPOSTf
	 */

	private void calTransition(Map<String, Integer> totalPrevPOSTf) {

		this.transition  = new Transition(this.table.size());

		Set<String> prevPosSet = grammar.getGrammar().keySet();
		for (String prevPos : prevPosSet) {
			Map<String,Integer> curPosMap = grammar.getGrammar().get(prevPos);
			Set<String> curPosSet = curPosMap.keySet();
			for (String curPos : curPosSet) {
				int prev2CurTf = curPosMap.get(curPos);
				int prevTf = totalPrevPOSTf.get(prevPos);
				if(curPos.equals("NNP")){
					prev2CurTf += 100000;
					prevTf += 100000;
				}
				double transitionScore = prev2CurTf/(double)prevTf;
				transitionScore = Math.log10(transitionScore);
				this.transition.put(this.table.getId(prevPos),this.table.getId(curPos),transitionScore);
			}
		}
	}

	/**
	 * Grammar에 포함되어 있는 이전 품사의 빈도수를 계산 <br>
	 * Grammar 파일 형식에 다음 품사의 빈도 수 합과 같음 <br>
	 * Grammar 파일 형식 <br>
	 * - <이전 품사> \t <다음 품사1:빈도>, <다음 품사2:빈도>, ...
	 * @return
	 */
	private Map<String, Integer> getTotalPrevPOSCount() {

		Map<String,Integer> posCountMap = new HashMap<String, Integer>();
		Set<String> prevPosSet = grammar.getGrammar().keySet();
		for (String prevPos : prevPosSet) {
			Map<String,Integer> prev2curPosMap = grammar.getGrammar().get(prevPos);
			Set<String> curPosSet = prev2curPosMap.keySet();
			for (String curPos : curPosSet) {
				Integer tf = posCountMap.get(prevPos);
				if(tf == null){
					tf = 0;
				}
				tf += prev2curPosMap.get(curPos);
				posCountMap.put(prevPos, tf);
			}
		}
		return posCountMap;

	}


	@Deprecated
	public void load(String path){
    }

	/**
	 * 본 메소드 호출 전에 반드시 buildPath 메소드를 실행해야합니다. </p>
	 * build된 모델들(transition, observation, pos_table)을 path 디렉토리에 저장합니다. </p>
	 * path 디렉토리에는 irregular.model, observation.model, pos.table, transition.model 파일이 생성됩니다.
	 * @param path 빌드된 데이터가 저장될 디렉토리
	 */
	public void save(String path){
		FileUtil.makePath(path);
		this.transition.save(path+File.separator+FILENAME.TRANSITION);
		this.observation.save(path+File.separator+FILENAME.OBSERVATION);
		this.table.save(path+File.separator+FILENAME.POS_TABLE);
		this.irrTrie.save(path+File.separator+FILENAME.IRREGULAR_MODEL);
	}

	/**
	 * 본 메소드는 반드시 buildPath 메소드 호출 전에만 사용가능합니다. </p>
	 * externalDic 경로에 있는 외부 사전을 추가하여 모델을 빌드합니다. </p>
	 * 외부 사전에 등록된 단어들의 빈도수는 50으로 초기화되어 모델에 반영됩니다.
	 *
	 * @param externalDic
	 */
	public void setExternalDic(String externalDic) {
		this.externalDic = externalDic;
	}
	private void addExternalDic(String filename) {
		try {
			if(filename != null) {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));
//				BufferedReader br = new BufferedReader(new FileReader(filename));
				String line = null;
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if (line.length() == 0 || line.charAt(0) == '#') continue;
					this.wordDic.append(line, "NNP",50);
				}
				br.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
