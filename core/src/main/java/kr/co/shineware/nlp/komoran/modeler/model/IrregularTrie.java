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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class IrregularTrie implements FileAccessible{
	private DoubleArrayAhoCorasick<List<IrregularNode>> daTrie;

	public IrregularTrie(){
		this.init();
	}

	public void init(){
		this.daTrie = new DoubleArrayAhoCorasick<>();
	}

	public void put(String irr,IrregularNode irrNode){
		List<IrregularNode> irrNodeList = this.daTrie.getValue(irr);
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
		this.daTrie.put(irr, irrNodeList);
	}

	public DoubleArrayAhoCorasick<List<IrregularNode>> getTrieDictionary(){
		return daTrie;
	}

	@Override
	public void save(String filename) {
		this.daTrie.save(filename);
	}

	@Override
	public void load(String filename) {
		this.daTrie.load(filename);
	}

	public void load(File file) {
		this.daTrie.load(file);
	}

	public void load(InputStream inputStream) {
		this.daTrie.load(inputStream);
	}
}
