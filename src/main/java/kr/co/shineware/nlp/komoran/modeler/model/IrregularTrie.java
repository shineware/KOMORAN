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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import kr.co.shineware.ds.aho_corasick.AhoCorasickDictionary;
import kr.co.shineware.nlp.komoran.interfaces.FileAccessible;

public class IrregularTrie implements FileAccessible{
	private AhoCorasickDictionary<List<IrregularNode>> dic;
	
	public IrregularTrie(){
		this.init();
	}

	public void init(){
		this.dic = null;
		this.dic = new AhoCorasickDictionary<>();
	}

	public void put(String irr,IrregularNode irrNode){
		List<IrregularNode> irrNodeList = this.dic.getValue(irr);
		if(irrNodeList == null){
			irrNodeList = new ArrayList<>();
			irrNodeList.add(irrNode);
		}else{
			boolean hasSameNode = false;
			for (IrregularNode irregularNode : irrNodeList) {
				if(irrNode.equals(irregularNode)){
					hasSameNode = true;
					break;
				}
			}
			if(!hasSameNode){
				irrNodeList.add(irrNode);
			}
		}
		this.dic.put(irr, irrNodeList);
	}
	
	public AhoCorasickDictionary<List<IrregularNode>> getTrieDictionary(){
		return dic;
	}

	@Override
	public void save(String filename) {
		this.dic.save(filename);
	}

	@Override
	public void load(String filename) {
		this.dic.load(filename);		
	}

	public void load(File file) {
		this.dic.load(file);
	}

	public void load(InputStream inputStream) {
		this.dic.load(inputStream);
	}
}
