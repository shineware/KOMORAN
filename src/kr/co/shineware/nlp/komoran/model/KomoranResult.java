package kr.co.shineware.nlp.komoran.model;

import java.util.ArrayList;
import java.util.List;

import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.core.model.LatticeNode;
import kr.co.shineware.util.common.model.Pair;


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
	
	public List<Token> getTokenInfoList(){
		List<Token> tokenList = new ArrayList<>();
		for (LatticeNode latticeNode : resultNodeList) {
			if(latticeNode.getMorphTag().getTag().equals(SYMBOL.END)){
				continue;
			}
			tokenList.add(new Token(latticeNode.getMorphTag().getMorph(), 
					latticeNode.getMorphTag().getTag(), latticeNode.getBeginIdx(), latticeNode.getEndIdx()));
		}
		return tokenList;
	}
	
	public List<Pair<String,String>> getList(){
		List<Pair<String,String>> resultList = new ArrayList<Pair<String,String>>();
		for (LatticeNode latticeNode : resultNodeList) {
			resultList.add(new Pair<>(latticeNode.getMorphTag().getMorph(),latticeNode.getMorphTag().getTag()));
		}
		return resultList;
	} 
}
