package kr.co.shineware.nlp.komoran.model;

import java.util.ArrayList;
import java.util.List;

import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.core.model.LatticeNode;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;
import kr.co.shineware.util.common.model.Pair;


public class KomoranResult{

	private List<LatticeNode> resultNodeList;
	private String jasoUnits;
	private KoreanUnitParser parser = new KoreanUnitParser();

	public KomoranResult(List<LatticeNode> latticeNode, String jasoUnits){
		this.resultNodeList = latticeNode;
		this.jasoUnits = jasoUnits;
	}
	
	public List<String> getNouns(){
		List<String> nounList = new ArrayList<>();
		for (LatticeNode latticeNode : resultNodeList) {
			if(latticeNode.getTag().equals(SYMBOL.NNG) || 
					latticeNode.getTag().equals(SYMBOL.NNP) ||
					latticeNode.getTag().equals(SYMBOL.NNG)){
				nounList.add(parser.combine(latticeNode.getMorph()));
			}
		}
		return nounList;
	}

	public String getPlainText(){
		StringBuffer result = new StringBuffer();
		for (LatticeNode latticeNode : resultNodeList) {
			if(latticeNode.getMorphTag().getTag().equals(SYMBOL.END)){
				continue;
			}
			result.append(parser.combine(latticeNode.getMorph())+"/"+latticeNode.getTag()+" ");
		}
		return result.toString().trim();
	}

	public List<Token> getTokenList(){
		List<Pair<Integer,Integer>> syllableAreaList = parser.getSyllableAreaList(this.jasoUnits);
		List<Token> tokenList = new ArrayList<>();
		int prevBeginIdx = 0;
		for (LatticeNode latticeNode : resultNodeList) {
			if(latticeNode.getMorphTag().getTag().equals(SYMBOL.END)){
				continue;
			}
			if(latticeNode.getBeginIdx() < 0){
				latticeNode.setBeginIdx(prevBeginIdx);
			}
			Pair<Integer,Integer> syllableArea = this.getSyllableArea(latticeNode.getBeginIdx(), latticeNode.getEndIdx(),syllableAreaList);
//			tokenList.add(new Token(latticeNode.getMorph(), 
//					latticeNode.getTag(), latticeNode.getBeginIdx(), latticeNode.getEndIdx()));
			tokenList.add(new Token(parser.combine(latticeNode.getMorph()), 
					parser.combine(latticeNode.getTag()), syllableArea.getFirst(), syllableArea.getSecond()));
			
			prevBeginIdx = latticeNode.getBeginIdx();
		}
		return tokenList;
	}

	private Pair<Integer, Integer> getSyllableArea(int jasoBeginIdx, int jasoEndIdx,
			List<Pair<Integer, Integer>> syllableAreaList) {
		Pair<Integer,Integer> syllableAreaPair = new Pair<Integer, Integer>();
		for(int i=0;i<syllableAreaList.size();i++){
			if(jasoBeginIdx >= syllableAreaList.get(i).getFirst()){
				syllableAreaPair.setFirst(i);
			}
			
			if(jasoEndIdx >= syllableAreaList.get(i).getSecond()){
				syllableAreaPair.setSecond(i+1);
			}
		}
		return syllableAreaPair;
	}

	public List<Pair<String,String>> getList(){
		List<Pair<String,String>> resultList = new ArrayList<Pair<String,String>>();
		for (LatticeNode latticeNode : resultNodeList) {
			if(latticeNode.getMorphTag().getTag().equals(SYMBOL.END)){
				continue;
			}
			resultList.add(new Pair<>(parser.combine(latticeNode.getMorph()),latticeNode.getTag()));
		}
		return resultList;
	} 
}
