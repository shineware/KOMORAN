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
package kr.co.shineware.nlp.komoran.modeler.model;

import kr.co.shineware.ds.aho_corasick.AhoCorasickDictionary;
import kr.co.shineware.nlp.komoran.core.model.DoubleArrayAhoCorasick;
import kr.co.shineware.nlp.komoran.interfaces.FileAccessible;
import kr.co.shineware.nlp.komoran.interfaces.UnitParser;
import kr.co.shineware.nlp.komoran.model.ScoredTag;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Observation implements FileAccessible {

	private DoubleArrayAhoCorasick<List<ScoredTag>> daTrie;
	private UnitParser parser;

	public Observation() {
		this.init();
	}

	private void init() {
		this.daTrie = new DoubleArrayAhoCorasick<>();
		this.parser = new KoreanUnitParser();
	}

	public void put(String word, String tag, int tagId, double observationScore) {
		String koreanUnits = parser.parse(word);
		List<ScoredTag> scoredTagList = this.daTrie.getValue(koreanUnits);
		if(scoredTagList == null){
			scoredTagList = new ArrayList<>();
			scoredTagList.add(new ScoredTag(tag, tagId, observationScore));
		}else{
			int i=0;
			for(i=0;i<scoredTagList.size();i++){
				if(scoredTagList.get(i).getTagId() == tagId){
					break;
				}
			}
			if(scoredTagList.size() == i){
				scoredTagList.add(new ScoredTag(tag, tagId, observationScore));
			}
		}
		this.daTrie.put(koreanUnits, scoredTagList);
	}

	public DoubleArrayAhoCorasick<List<ScoredTag>> getTrieDictionary(){
		return daTrie;
	}

	/**
	 * @deprecated Legacy 호환용. getTrieDictionary()를 사용할 것.
	 */
	@Deprecated
	public AhoCorasickDictionary<List<ScoredTag>> getLegacyTrieDictionary(){
		return daTrie.getLegacyDictionary();
	}

	@Override
	public void save(String filename) {
		daTrie.save(filename);
	}

	@Override
	public void load(String filename) {
		daTrie.load(filename);
	}

	public void load(File file) {
		daTrie.load(file);
	}

	public void load(InputStream is) {
		daTrie.load(is);
	}
}
