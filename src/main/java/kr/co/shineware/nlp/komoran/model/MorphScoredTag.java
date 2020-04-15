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
package kr.co.shineware.nlp.komoran.model;

public class MorphScoredTag extends ScoredTag{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String morph;
	
	public MorphScoredTag(String tag, int tagId, double score, String morph) {
		super(tag, tagId, score);
		this.morph = morph;
	}
	public String getMorph() {
		return morph;
	}
	public void setMorph(String morph) {
		this.morph = morph;
	}
	
}
