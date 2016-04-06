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

	public List<Token> getTokenInfo(){
		return this.convertTokenInfoList();
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

	private List<Token> convertTokenInfoList() {
		int curSyllableIdx = 0;
		int beginIdx = 0;
		int endIdx = 0;
		List<Token> tokenInfos = new ArrayList<>();
		for (Pair<String, String> morphPosPair : this.getList()) {
			if(morphPosPair.getFirst().equals(SYMBOL.END)){
				curSyllableIdx++;
				continue;
			}
			//가장 앞 문자가 자소 단위로 분할된 경우
			if(this.startWithJamo(morphPosPair.getFirst())){
				beginIdx = curSyllableIdx-1;
				if(beginIdx < 0){
					beginIdx = 0;
				}
				endIdx = beginIdx + morphPosPair.getFirst().length();
			}
			//자소 단위가 아닌 경우
			else {
				beginIdx = curSyllableIdx;
				endIdx = beginIdx + morphPosPair.getFirst().length();
			}

			tokenInfos.add(new Token(morphPosPair.getFirst(),morphPosPair.getSecond(),beginIdx,endIdx));
			curSyllableIdx = endIdx;
		}
//		System.out.println(tokenInfos);
		return tokenInfos;
	}

	private boolean startWithJamo(String morph) {

		char ch = morph.charAt(0);
		Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(ch);
		if(unicodeBlock == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO){
			return true;
		}
		return false;
	}

	public String getPlainText(){
		StringBuffer result = new StringBuffer();
		for (LatticeNode latticeNode : resultNodeList) {
			if(latticeNode.getMorphTag().getTag().equals(SYMBOL.END)){
				continue;
			}
			result.append(latticeNode.getMorph()+"/"+latticeNode.getTag()+" ");
		}
		return result.toString().trim();
	}

	public List<Token> getTokenInfoList(){
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
			resultList.add(new Pair<>(parser.combine(latticeNode.getMorph()),latticeNode.getTag()));
		}
		return resultList;
	} 
}
