/*
 * KOMORAN 2.0 - Korean Morphology Analyzer
 *
 * Copyright 2014 Shineware http://www.shineware.co.kr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kr.co.shineware.nlp.komoran.modeler.model;

import java.util.ArrayList;
import java.util.List;

import kr.co.shineware.ds.aho_corasick.AhoCorasickDictionary;
import kr.co.shineware.nlp.komoran.interfaces.FileAccessible;
import kr.co.shineware.nlp.komoran.interfaces.UnitParser;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;
import kr.co.shineware.util.common.model.Pair;

public class Observation implements FileAccessible{

	private AhoCorasickDictionary<List<Pair<Integer,Double>>> observation;
	private UnitParser parser;
	
	public Observation(){
		this.init();
	}
	
	private void init() {
		this.observation = new AhoCorasickDictionary<>();
		this.parser = new KoreanUnitParser();
	}

	public void put(String word, int id, double observationScore) {
		String koreanUnits = parser.parse(word);
		List<Pair<Integer,Double>> posIdScorePairList = this.observation.getValue(koreanUnits);
		if(posIdScorePairList == null){
			posIdScorePairList = new ArrayList<>();
			posIdScorePairList.add(new Pair<Integer, Double>(id, observationScore));
		}else{
			int i=0;
			for(i=0;i<posIdScorePairList.size();i++){
				if(posIdScorePairList.get(i).getFirst() == id){
					break;
				}
			}
			if(posIdScorePairList.size() == i){
				posIdScorePairList.add(new Pair<Integer, Double>(id, observationScore));
			}
		}
		this.observation.put(koreanUnits, posIdScorePairList);
	}
	
	public AhoCorasickDictionary<List<Pair<Integer,Double>>> getTrieDictionary(){
		return observation;
	}

	@Override
	public void save(String filename) {
		observation.save(filename);
	}

	@Override
	public void load(String filename) {
		observation.load(filename);	
	}
}
