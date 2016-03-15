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
package kr.co.shineware.nlp.komoran.core.model;

import kr.co.shineware.nlp.komoran.model.MorphTag;

public class LatticeNode {
	
	@Override
	public String toString() {
		return "LatticeNode [beginIdx=" + beginIdx + ", endIdx=" + endIdx
				+ ", morphTag=" + morphTag + ", score=" + score
				+ ", prevNodeHash=" + prevNodeHash + "]"
				+ ", nodeHash=" + this.hashCode() + "]";
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + beginIdx;
		result = prime * result + endIdx;
		result = prime * result
				+ ((morphTag == null) ? 0 : morphTag.hashCode());
		result = prime * result + prevNodeHash;
		long temp;
		temp = Double.doubleToLongBits(score);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LatticeNode other = (LatticeNode) obj;
		if (beginIdx != other.beginIdx)
			return false;
		if (endIdx != other.endIdx)
			return false;
		if (morphTag == null) {
			if (other.morphTag != null)
				return false;
		} else if (!morphTag.equals(other.morphTag))
			return false;
		if (prevNodeHash != other.prevNodeHash)
			return false;
		if (Double.doubleToLongBits(score) != Double
				.doubleToLongBits(other.score))
			return false;
		return true;
	}



	private int beginIdx;
	private int endIdx;
	private MorphTag morphTag;
	private double score;
//	private int prevNodeIdx=-1;
	private int prevNodeHash=-1;
	
	public LatticeNode(){
		;
	}
	
	public LatticeNode(int beginIdx, int endIdx, MorphTag morphTag, double score) {
		this.beginIdx = beginIdx;
		this.endIdx = endIdx;
		this.morphTag = morphTag;
		this.score = score;
	}
	public LatticeNode(LatticeNode latticeNode) {
		this.beginIdx = latticeNode.getBeginIdx();
		this.endIdx = latticeNode.getEndIdx();
		this.morphTag = latticeNode.getMorphTag();
		this.score = latticeNode.getScore();
	}

	public int getBeginIdx() {
		return beginIdx;
	}
	public void setBeginIdx(int beginIdx) {
		this.beginIdx = beginIdx;
	}
	public int getEndIdx() {
		return endIdx;
	}
	public void setEndIdx(int endIdx) {
		this.endIdx = endIdx;
	}
	public MorphTag getMorphTag() {
		return morphTag;
	}
	public void setMorphTag(MorphTag morphTag) {
		this.morphTag = morphTag;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}

	public int getPrevNodeHash() {
		return prevNodeHash;
	}

	public void setPrevNodeHash(int prevNodeHash) {
		this.prevNodeHash = prevNodeHash;
	}
}
