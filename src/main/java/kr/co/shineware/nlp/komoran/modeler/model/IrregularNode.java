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

import java.io.Serializable;
import java.util.List;

import kr.co.shineware.util.common.model.Pair;

public class IrregularNode implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String lastMorph;
	private int firstPosId;
	private int lastPosId;
	private double innerScore;
	private List<Pair<String, Integer>> irrNodeTokens;
	private String morphFormat;
	
	public String getLastMorph() {
		return lastMorph;
	}
	public void setLastMorph(String lastMorph) {
		this.lastMorph = lastMorph;
	}
	public double getInnerScore() {
		return innerScore;
	}
	public void setInnerScore(double innerScore) {
		this.innerScore = innerScore;
	}
	public int getFirstPosId() {
		return firstPosId;
	}
	public void setFirstPosId(int firstPosId) {
		this.firstPosId = firstPosId;
	}	
	public int getLastPosId() {
		return lastPosId;
	}
	public void setLastPosId(int lastPosId) {
		this.lastPosId = lastPosId;
	}	
	public void setTokens(List<Pair<String, Integer>> irrNodeTokens) {
		this.irrNodeTokens = irrNodeTokens;		
	}
	public List<Pair<String, Integer>> getTokens() {
		return this.irrNodeTokens;
	}
	public void setMorphFormat(String morphFormat) {
		this.morphFormat = morphFormat;		
	}
	public String getMorphFormat(){
		return morphFormat;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + firstPosId;
		long temp;
		temp = Double.doubleToLongBits(innerScore);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((irrNodeTokens == null) ? 0 : irrNodeTokens.hashCode());
		result = prime * result
				+ ((lastMorph == null) ? 0 : lastMorph.hashCode());
		result = prime * result + lastPosId;
		result = prime * result
				+ ((morphFormat == null) ? 0 : morphFormat.hashCode());
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
		IrregularNode other = (IrregularNode) obj;
		if (firstPosId != other.firstPosId)
			return false;
		if (Double.doubleToLongBits(innerScore) != Double
				.doubleToLongBits(other.innerScore))
			return false;
		if (irrNodeTokens == null) {
			if (other.irrNodeTokens != null)
				return false;
		} else if (!irrNodeTokens.equals(other.irrNodeTokens))
			return false;
		if (lastMorph == null) {
			if (other.lastMorph != null)
				return false;
		} else if (!lastMorph.equals(other.lastMorph))
			return false;
		if (lastPosId != other.lastPosId)
			return false;
		if (morphFormat == null) {
            return other.morphFormat == null;
		} else return morphFormat.equals(other.morphFormat);
    }
	@Override
	public String toString() {
		return "IrregularNode [lastMorph=" + lastMorph + ", firstPosId="
				+ firstPosId + ", lastPosId=" + lastPosId + ", innerScore="
				+ innerScore + ", irrNodeTokens=" + irrNodeTokens
				+ ", morphFormat=" + morphFormat + "]";
	}
	
	
	
}
