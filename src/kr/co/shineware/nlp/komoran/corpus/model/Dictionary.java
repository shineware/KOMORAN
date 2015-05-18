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
package kr.co.shineware.nlp.komoran.corpus.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import kr.co.shineware.nlp.komoran.interfaces.FileAccessible;
import kr.co.shineware.util.common.collection.MapUtil;

/**
 * This class is model of dictionary. It can be used like DB instead of save, load.
 * @author Junsoo Shin <jsshin@shineware.co.kr> 
 * @version 2.1
 * @since 2.1
 */
public class Dictionary implements FileAccessible{

	//key = word
	//value.key = pos
	//value.value = tf
	private Map<String,Map<String,Integer>> dictionary;

	/**
	 * 사전 생성자로써 init() method 실행.
	 */
	public Dictionary(){
		this.init();
	}

	/**
	 * 사전 생성자로써 filename에 저장된 사전 데이터를 로딩
	 * 데이터 로딩 시 init method 실행
	 * @param filename
	 */
	public Dictionary(String filename){
		this.load(filename);
	}

	/**
	 * 사전이 사용되는 메모리 초기화.
	 * 내부적으로 Double Map collection 사용됨.
	 */
	protected void init(){
		this.dictionary = new HashMap<String,Map<String, Integer>>();
	}

	/**
	 * 현재 메모리에 올라가 있는 사전 데이터를 반환. 
	 * @return
	 */
	public Map<String, Map<String, Integer>> getDictionary(){
		return dictionary;
	}

	@Override
	public void save(String filename) {
		try {

			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			Set<Entry<String, Map<String,Integer>>> entrySet = dictionary.entrySet();
			for (Entry<String, Map<String, Integer>> entry : entrySet) {

				bw.write(entry.getKey());
				bw.write("\t");

				Set<String> posSet = entry.getValue().keySet();
				int posSize = posSet.size();
				int count = 0;
				for (String pos : posSet) {
					bw.write(pos);
					bw.write(":");
					Integer tf = entry.getValue().get(pos);
					bw.write(""+tf);
					count++;
					if(count != posSize)
						bw.write("\t");
				}
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void load(String filename){
		this.init();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line = null;
			while((line = br.readLine()) != null){
				line = line.trim();
				String[] tokens = line.split("\t");				
				//단어
				String word = tokens[0];
				
				//품사 및 빈도 정보
				Map<String, Integer> posTfMap = new HashMap<String,Integer>();
				for(int i=1;i<tokens.length;i++){
					String token = tokens[i];
					int separatorIdx = token.lastIndexOf(':');
					String pos = token.substring(0, separatorIdx);
					int tf = Integer.parseInt(token.substring(separatorIdx+1));
					posTfMap.put(pos, tf);
				}
				dictionary.put(word, posTfMap);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 단어의 품사 리스트를 반환
	 * @param word
	 * @return
	 */
	protected List<String> get(String word) {
		Map<String,Integer> keyMap = dictionary.get(word);
		if(keyMap == null){
			return null;
		}
		return MapUtil.getKeyList(keyMap);
	}

	/**
	 * 사전에 형태소, 품사를 추가 <br>
	 * overwrite형식이 아닌 append 형식으로써 단어에 따른 품사의 빈도수를 증가시킴
	 * @param word
	 * @param pos
	 */
	public void append(String word, String pos) {
		Map<String,Integer> posMap = dictionary.get(word);
		if(posMap == null){
			posMap = new HashMap<String, Integer>();
		}
		Integer tf = posMap.get(pos);
		if(tf == null){
			tf = 0;
		}
		tf++;
		posMap.put(pos, tf);
		dictionary.put(word, posMap);
	}

	/**
	 * 단어의 품사 리스트를 반환
	 * @param word
	 * @return
	 */
	public List<String> getPosList(String word){
		return this.get(word);
	}
}
