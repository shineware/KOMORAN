package kr.co.shineware.nlp.komoran.model;

import java.util.List;

import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.core.model.LatticeNode;


public class KomoranResult{
	private List<LatticeNode> resultNodeList;
	public KomoranResult(List<LatticeNode> latticeNode){
		this.resultNodeList = latticeNode;
	}
	public String getPlainText(){
		StringBuffer result = new StringBuffer();
		for (LatticeNode latticeNode : resultNodeList) {
			if(latticeNode.getMorphTag().getTag().equals(SYMBOL.END)){
				continue;
			}
			result.append(latticeNode.getMorphTag().getMorph()+"/"+latticeNode.getMorphTag().getTag()+" ");
		}
		return result.toString().trim();
	}
}
