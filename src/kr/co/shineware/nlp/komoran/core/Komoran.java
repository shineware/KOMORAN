package kr.co.shineware.nlp.komoran.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.co.shineware.nlp.komoran.constant.SCORE;
import kr.co.shineware.nlp.komoran.core.model.Lattice;
import kr.co.shineware.nlp.komoran.core.model.LatticeNode;
import kr.co.shineware.nlp.komoran.core.model.Resources;
import kr.co.shineware.nlp.komoran.model.MorphTag;
import kr.co.shineware.nlp.komoran.model.ScoredTag;
import kr.co.shineware.nlp.komoran.model.Tag;
import kr.co.shineware.nlp.komoran.modeler.model.IrregularNode;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;
import kr.co.shineware.util.checker.time.TimeChecker;
import kr.co.shineware.util.common.model.Pair;
import kr.co.shineware.util.common.string.StringUtil;

public class Komoran {

	public Resources resources;
	private KoreanUnitParser unitParser;
	private Lattice lattice;

	private String prevPos;
	private String prevMorph;
	private int prevBeginIdx;

	public Komoran(String modelPath){
		this.resources = new Resources();
		this.load(modelPath);
		this.unitParser = new KoreanUnitParser();
	}

	public List<ScoredTag> getObservationScore(String key){
		return this.resources.getObservation().getTrieDictionary().getValue(this.unitParser.parse(key));
	}

	public Double getTransitionScore(String prevPos,String nextPos){
		return this.resources.getTransition().get(this.resources.getTable().getId(prevPos),
				this.resources.getTable().getId(nextPos));
	}

	public List<Pair<String,String>> analyze(String token){
		List<Pair<String,String>> resultList = new ArrayList<>();

		token = token.trim();
		if(token.length() == 0){
			return null;
		}

		this.resources.getIrrTrie().getTrieDictionary().initCurrentNode();
		this.resources.getObservation().getTrieDictionary().initCurrentNode();

		this.lattice = new Lattice(this.resources);
		this.lattice.setUnitParser(this.unitParser);

		//연속된 숫자, 외래어, 기호 등을 파싱 하기 위한 버퍼
		this.prevPos = "";
		this.prevMorph = "";
		this.prevBeginIdx = 0;

		//자소 단위로 분할
		String jasoUnits = unitParser.parse(token);

		int length = jasoUnits.length();

		for(int i=0; i<length; i++){
			this.numericParsing(jasoUnits.charAt(i),i); //숫자 파싱
			//				this.foreignParsing(jasoUnits.charAt(i),i); //외래어 파
			this.regularParsing(jasoUnits.charAt(i),i); //일반규칙 파싱
			
//			TimeChecker.setBeginFlag("IRREGULAR_PARSE");
			this.irregularParsing(jasoUnits.charAt(i),i); //불규칙 파싱
//			TimeChecker.setEndFlag("IRREGULAR_PARSE");
//			TimeChecker.appendElapsedTime("IRREGULAR_PARSE", TimeChecker.getElapsedTime("IRREGULAR_PARSE"));
//			
//			TimeChecker.setBeginFlag("IRREGULAR_EXT_PARSE");
			this.irregularExtends(jasoUnits.charAt(i),i);
//			TimeChecker.setEndFlag("IRREGULAR_EXT_PARSE");
//			TimeChecker.appendElapsedTime("IRREGULAR_EXT_PARSE", TimeChecker.getElapsedTime("IRREGULAR_EXT_PARSE"));
		}

		this.consumeRuleParserBuffer(jasoUnits);

		this.lattice.setLastIdx(jasoUnits.length());
		this.lattice.appendEndNode();
		//			this.lattice.printLattice();

		List<Pair<String,String>> shortestPathList = this.lattice.findPath();

		//미분석인 경우
		if(shortestPathList == null){
			resultList.add(new Pair<>(token,"NA"));
		}else{
			Collections.reverse(shortestPathList);
			resultList.addAll(shortestPathList);
		}		

		return resultList;
	}

	private void numericParsing(char charAt, int i) {
		String curPos = "";
		if(StringUtil.isEnglish(charAt)){
			curPos = "SL";
		}else if(StringUtil.isNumeric(charAt)){
			curPos = "SN";
		}else if(StringUtil.isChinese(charAt)){
			curPos = "SH";
		}else if(StringUtil.isForeign(charAt)){
			curPos = "SL";
		}

		if(curPos.equals(this.prevPos)){
			this.prevMorph += charAt;
		}
		else{
			if(this.prevPos.equals("SL")){
				this.lattice.put(this.prevBeginIdx, i, this.prevMorph, new Tag(this.prevPos,this.resources.getTable().getId(this.prevPos)), SCORE.SL);
			}else if(this.prevPos.equals("SN")){
				this.lattice.put(this.prevBeginIdx, i, this.prevMorph, new Tag(this.prevPos,this.resources.getTable().getId(this.prevPos)), SCORE.SN);
			}else if(this.prevPos.equals("SH")){
				this.lattice.put(this.prevBeginIdx, i, this.prevMorph, new Tag(this.prevPos,this.resources.getTable().getId(this.prevPos)), SCORE.SH);
			}

			this.prevBeginIdx = i;
			this.prevMorph = ""+charAt;
			this.prevPos = curPos;
		}
	}

	private void consumeRuleParserBuffer(String in) {
		if(this.prevPos.trim().length() != 0){
			if(this.prevPos.equals("SL")){
				this.lattice.put(this.prevBeginIdx, in.length(), this.prevMorph, new Tag(this.prevPos,this.resources.getTable().getId(this.prevPos)), SCORE.SL);
			}else if(this.prevPos.equals("SH")){
				this.lattice.put(this.prevBeginIdx, in.length(), this.prevMorph, new Tag(this.prevPos,this.resources.getTable().getId(this.prevPos)), SCORE.SH);
			}else if(this.prevPos.equals("SN")){
				this.lattice.put(this.prevBeginIdx, in.length(), this.prevMorph, new Tag(this.prevPos,this.resources.getTable().getId(this.prevPos)), SCORE.SN);
			}
		}
	}

	private void foreignParsing(char charAt, int i) {
		;
	}

	private void irregularExtends(char jaso, int curIndex) {
		List<LatticeNode> prevLatticeNodes = this.lattice.getNodeList(curIndex);
		if(prevLatticeNodes == null){
			;
		}else{
			List<LatticeNode> extendedIrrNodeList = new ArrayList<>();

			for (LatticeNode prevLatticeNode : prevLatticeNodes) {
				//불규칙 태그인 경우에 대해서만
				if( prevLatticeNode.getMorphTag().getTagId() == -1 ) {
					//형태소를 공백으로 분할 ('흐르/VV 어'와 같은 형태로 되어 있기 때문) 
					String[] tokens = prevLatticeNode.getMorphTag().getMorph().split(" ");
					//마지막 형태소 정보를 얻어옴
					String lastMorph = tokens[tokens.length-1];

					//불규칙의 마지막 형태소에 현재 자소 단위를 합쳤을 때 자식 노드가 있다면 계속 탐색 가능 후보로 처리 해야함
					if(this.resources.getObservation().getTrieDictionary().hasChild((lastMorph+jaso).toCharArray())){
						//						System.out.println(lastMorph+"+"+jaso+" has child!");
						LatticeNode extendedIrregularNode = new LatticeNode();
						extendedIrregularNode.setBeginIdx(prevLatticeNode.getBeginIdx());
						extendedIrregularNode.setEndIdx(curIndex+1);
						extendedIrregularNode.setMorphTag(new MorphTag(prevLatticeNode.getMorphTag().getMorph()+jaso, "IRR", -1));
						extendedIrregularNode.setPrevNodeIdx(prevLatticeNode.getPrevNodeIdx());
						extendedIrregularNode.setScore(prevLatticeNode.getScore());
						extendedIrrNodeList.add(extendedIrregularNode);
					}
					//불규칙의 마지막 형태소에 현재 자소 단위를 합쳐 점수를 얻어옴
					//					System.out.println(lastMorph+"+"+jaso+" get score!");
					List<ScoredTag> lastScoredTags = this.resources.getObservation().getTrieDictionary().getValue(lastMorph+jaso);
					if(lastScoredTags == null){
						continue;
					}

					for (ScoredTag scoredTag : lastScoredTags) {
						this.lattice.put(prevLatticeNode.getBeginIdx(), curIndex+1, prevLatticeNode.getMorphTag().getMorph()+jaso+"/"+scoredTag.getTag());
					}
				}
			}
			for (LatticeNode extendedIrrNode : extendedIrrNodeList) {
				this.lattice.appendNode(extendedIrrNode);
			}

		}
	}

	private boolean irregularParsing(char jaso, int curIndex) {
		//불규칙 노드들을 얻어옴
		Map<String,List<IrregularNode>> morphIrrNodesMap = this.getIrregularNodes(jaso);
		if(morphIrrNodesMap == null){
			return false;
		}


		//형태소 정보만 얻어옴
		Set<String> morphs = morphIrrNodesMap.keySet();
		for (String morph : morphs) {
			List<IrregularNode> irrNodes = morphIrrNodesMap.get(morph);
			int beginIdx = curIndex-morph.length()+1;
			int endIdx = curIndex+1;
			for (IrregularNode irregularNode : irrNodes) {
				this.insertLattice(beginIdx, endIdx, irregularNode);
			}
		}

		return true;
	}

	private void insertLattice(int beginIdx, int endIdx,
			IrregularNode irregularNode) {
		this.lattice.put(beginIdx, endIdx, irregularNode);		
	}

	private boolean regularParsing(char jaso,int curIndex) {
		//TRIE 기반의 사전 검색하여 형태소와 품사 및 품사 점수(observation)를 얻어옴

		Map<String, List<ScoredTag>> morphScoredTagsMap = this.getMorphScoredTagsMap(jaso);

		if(morphScoredTagsMap == null){
			return false;
		}
		
		TimeChecker.setBeginFlag("REGULAR_INSERT");
		
		//key는 형태소
		//value는 형태소가 갖는 품사별 관측확률(점수)
//		Set<Entry<String,List<ScoredTag>>> entrySet = morphScoredTagsMap.entrySet();
//		for (Entry<String, List<ScoredTag>> entry : entrySet) {
//			int beginIdx = curIndex-entry.getKey().length()+1;
//			int endIdx = curIndex+1;
//			for(ScoredTag scoredTag : entry.getValue()){
//				this.insertLattice(beginIdx,endIdx,entry.getKey(),scoredTag,scoredTag.getScore());
//			}
//		}

		//형태소 정보만 얻어옴

		Set<String> morphes = this.getMorphes(morphScoredTagsMap);

		//각 형태소와 품사 정보를 lattice에 삽입
		for (String morph : morphes) {
			int beginIdx = curIndex-morph.length()+1;
			int endIdx = curIndex+1;

			//형태소에 대한 품사 및 점수(observation) 정보를 List 형태로 가져옴
			List<ScoredTag> scoredTags = morphScoredTagsMap.get(morph);
			for (ScoredTag scoredTag : scoredTags) {
				this.insertLattice(beginIdx,endIdx,morph,scoredTag,scoredTag.getScore());
			}
		}
		return true;
	}

	private Map<String, List<IrregularNode>> getIrregularNodes(char jaso) {
		return this.resources.getIrrTrie().getTrieDictionary().get(jaso);
	}

	private void insertLattice(int beginIdx, int endIdx, String morph,
			Tag tag, double score) {
		this.lattice.put(beginIdx,endIdx,morph,tag,score);
	}

	private Set<String> getMorphes(
			Map<String, List<ScoredTag>> morphScoredTagMap) {
		return morphScoredTagMap.keySet();
	}

	private Map<String, List<ScoredTag>> getMorphScoredTagsMap(char jaso) {
		return this.resources.getObservation().getTrieDictionary().get(jaso);
	}

	public void load(String modelPath){
		this.resources.load(modelPath);
	}
}
