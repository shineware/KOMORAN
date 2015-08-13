package kr.co.shineware.nlp.komoran.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.co.shineware.nlp.komoran.constant.SCORE;
import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.core.model.Lattice;
import kr.co.shineware.nlp.komoran.core.model.LatticeNode;
import kr.co.shineware.nlp.komoran.core.model.Resources;
import kr.co.shineware.nlp.komoran.corpus.parser.CorpusParser;
import kr.co.shineware.nlp.komoran.corpus.parser.model.ProblemAnswerPair;
import kr.co.shineware.nlp.komoran.model.MorphTag;
import kr.co.shineware.nlp.komoran.model.ScoredTag;
import kr.co.shineware.nlp.komoran.model.Tag;
import kr.co.shineware.nlp.komoran.modeler.model.IrregularNode;
import kr.co.shineware.nlp.komoran.modeler.model.Observation;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;
import kr.co.shineware.util.common.model.Pair;
import kr.co.shineware.util.common.string.StringUtil;

public class Komoran {

	private Resources resources;
	private Observation userDic;
	private KoreanUnitParser unitParser;
	private Lattice lattice;

	private HashMap<String, List<Pair<String, String>>> fwd;

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

	public List<Pair<String,String>> analyze(String sentence){
		List<Pair<String,String>> resultList = new ArrayList<>();

		String[] tokens = sentence.split(" ");
		for (String token : tokens) {

			token = token.trim();
			if(token.length() == 0){
				return null;
			}

			//기분석 사전
			List<Pair<String,String>> fwdLookupResult = this.lookupFwd(token);
			if(fwdLookupResult != null){
				resultList.addAll(fwdLookupResult);
			}

			this.resources.getIrrTrie().getTrieDictionary().initCurrentNode();
			this.resources.getObservation().getTrieDictionary().initCurrentNode();
			if(this.userDic != null){
				this.userDic.getTrieDictionary().initCurrentNode();
			}

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
				this.symbolParsing(jasoUnits.charAt(i),i); //숫자 파싱
				this.userDicParsing(jasoUnits.charAt(i),i); //사용자 사전 적용
				this.regularParsing(jasoUnits.charAt(i),i); //일반규칙 파싱
				this.irregularParsing(jasoUnits.charAt(i),i); //불규칙 파싱
				this.irregularExtends(jasoUnits.charAt(i),i); //불규칙 확장
			}

			this.consumeRuleParserBuffer(jasoUnits);

			this.lattice.setLastIdx(jasoUnits.length());
			this.lattice.appendEndNode();

			List<Pair<String,String>> shortestPathList = this.lattice.findPath();

			//미분석인 경우
			if(shortestPathList == null){
				resultList.add(new Pair<>(token,"NA"));
			}else{
				Collections.reverse(shortestPathList);
				resultList.addAll(shortestPathList);
			}		
		}

		return resultList;
	}

	private boolean userDicParsing(char jaso, int curIndex) {
		//TRIE 기반의 사전 검색하여 형태소와 품사 및 품사 점수(observation)를 얻어옴
		Map<String, List<ScoredTag>> morphScoredTagsMap = this.getMorphScoredTagMapFromUserDic(jaso);

		if(morphScoredTagsMap == null){
			return false;
		}

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

	private List<Pair<String, String>> lookupFwd(String token) {
		if(this.fwd == null){
			return null;
		}
		return this.fwd.get(token);
	}

	private void symbolParsing(char charAt, int i) {
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
				this.lattice.put(this.prevBeginIdx, i, this.prevMorph, this.prevPos,this.resources.getTable().getId(this.prevPos), SCORE.SL);
			}else if(this.prevPos.equals("SN")){
				this.lattice.put(this.prevBeginIdx, i, this.prevMorph, this.prevPos,this.resources.getTable().getId(this.prevPos), SCORE.SN);
			}else if(this.prevPos.equals("SH")){
				this.lattice.put(this.prevBeginIdx, i, this.prevMorph, this.prevPos,this.resources.getTable().getId(this.prevPos), SCORE.SH);
			}

			this.prevBeginIdx = i;
			this.prevMorph = ""+charAt;
			this.prevPos = curPos;
		}
	}

	private void consumeRuleParserBuffer(String in) {
		if(this.prevPos.trim().length() != 0){
			if(this.prevPos.equals("SL")){
				this.lattice.put(this.prevBeginIdx, in.length(), this.prevMorph, this.prevPos,this.resources.getTable().getId(this.prevPos), SCORE.SL);
			}else if(this.prevPos.equals("SH")){
				this.lattice.put(this.prevBeginIdx, in.length(), this.prevMorph, this.prevPos,this.resources.getTable().getId(this.prevPos), SCORE.SH);
			}else if(this.prevPos.equals("SN")){
				this.lattice.put(this.prevBeginIdx, in.length(), this.prevMorph, this.prevPos,this.resources.getTable().getId(this.prevPos), SCORE.SN);
			}
		}
	}
	
	private void irregularExtends(char jaso, int curIndex) {
		List<LatticeNode> prevLatticeNodes = this.lattice.getNodeList(curIndex);
		if(prevLatticeNodes == null){
			;
		}else{
			Set<LatticeNode> extendedIrrNodeList = new HashSet<>();

			for (LatticeNode prevLatticeNode : prevLatticeNodes) {
				//불규칙 태그인 경우에 대해서만
				if( prevLatticeNode.getMorphTag().getTagId() == -1 ) {
					//마지막 형태소 정보를 얻어옴
					String lastMorph = prevLatticeNode.getMorphTag().getMorph();

					//불규칙의 마지막 형태소에 현재 자소 단위를 합쳤을 때 자식 노드가 있다면 계속 탐색 가능 후보로 처리 해야함
					if(this.resources.getObservation().getTrieDictionary().hasChild((lastMorph+jaso).toCharArray())){
						LatticeNode extendedIrregularNode = new LatticeNode();
						extendedIrregularNode.setBeginIdx(prevLatticeNode.getBeginIdx());
						extendedIrregularNode.setEndIdx(curIndex+1);
						extendedIrregularNode.setMorphTag(new MorphTag(prevLatticeNode.getMorphTag().getMorph()+jaso, "IRR", -1));
						extendedIrregularNode.setPrevNodeIdx(prevLatticeNode.getPrevNodeIdx());
						extendedIrregularNode.setScore(prevLatticeNode.getScore());
						extendedIrrNodeList.add(extendedIrregularNode);
					}
					//불규칙의 마지막 형태소에 현재 자소 단위를 합쳐 점수를 얻어옴
					List<ScoredTag> lastScoredTags = this.resources.getObservation().getTrieDictionary().getValue(lastMorph+jaso);
					if(lastScoredTags == null){
						continue;
					}

					//얻어온 점수를 토대로 계산
					for (ScoredTag scoredTag : lastScoredTags) {
						this.lattice.put(prevLatticeNode.getBeginIdx(), curIndex+1, prevLatticeNode.getMorphTag().getMorph()+jaso,
								scoredTag.getTag(), scoredTag.getTagId(),scoredTag.getScore());
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
		this.lattice.put(beginIdx,endIdx,morph,tag.getTag(),tag.getTagId(),score);
	}

	private Set<String> getMorphes(
			Map<String, List<ScoredTag>> morphScoredTagMap) {
		return morphScoredTagMap.keySet();
	}

	private Map<String, List<ScoredTag>> getMorphScoredTagsMap(char jaso) {
		return this.resources.getObservation().getTrieDictionary().get(jaso);
	}
	private Map<String, List<ScoredTag>> getMorphScoredTagMapFromUserDic(char jaso){
		if(this.userDic == null){
			return null;
		}
		return this.userDic.getTrieDictionary().get(jaso);
	}

	public void load(String modelPath){
		this.resources.load(modelPath);
	}

	//기분석 사전
	public void setFWDic(String filename) {		
		try {
			CorpusParser corpusParser = new CorpusParser();
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			this.fwd = new HashMap<String, List<Pair<String, String>>>();
			while ((line = br.readLine()) != null) {
				String[] tmp = line.split("\t");
				if (tmp.length != 2 || tmp[0].charAt(0) == '#'){
					tmp = null;
					continue;
				}
				ProblemAnswerPair problemAnswerPair = corpusParser.parse(line);
				List<Pair<String,String>> convertAnswerList = new ArrayList<>();
				for (Pair<String, String> pair : problemAnswerPair.getAnswerList()) {
					convertAnswerList.add(
							new Pair<String, String>(pair.getFirst(), pair.getSecond()));
				}

				this.fwd.put(problemAnswerPair.getProblem(),
						convertAnswerList);
				tmp = null;
				problemAnswerPair = null;
				convertAnswerList = null;
			}			
			br.close();

			//init
			corpusParser = null;
			br = null;
			line = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setUserDic(String userDic) {
		try {
			//			System.out.println("User dic loading : "+new File(userDic).getAbsolutePath());
			this.userDic = new Observation();
			BufferedReader br = new BufferedReader(new FileReader(userDic));
			String line = null;
			while((line = br.readLine()) != null){
				line = line.trim();				
				if(line.length() == 0 || line.charAt(0) == '#')continue;
				int lastIdx = line.lastIndexOf("\t");

				String morph;
				String pos;
				if(lastIdx == -1){
					morph = line.trim();
					pos = "NNP";
				}else{
					morph = line.substring(0, lastIdx);
					pos = line.substring(lastIdx+1);
				}
				this.userDic.put(morph, pos, this.resources.getTable().getId(pos), 0.0);

				line = null;
				morph = null;
				pos = null;
			}
			br.close();

			//init
			br = null;
			line = null;
			this.userDic.getTrieDictionary().buildFailLink();
		} catch (Exception e) {		
			e.printStackTrace();
		}		
	}

	//for debug
	public double getScore(String src) {
		String[] tokens = src.split(" ");
		double score = 0.0;
		String prevTag = SYMBOL.START;
		for(int i=0;i<tokens.length;i++){
			String morph = tokens[i];
			String pos = tokens[i+1];
			i++;
			
			for (ScoredTag tag : this.getObservationScore(morph)) {
				if(tag.getTag().equals(pos)){
					score += tag.getScore();
					break;
				}
			}
			
			score += this.getTransitionScore(prevTag, pos);
			prevTag = pos;
		}
		score += this.getTransitionScore(prevTag, SYMBOL.END);
		return score;
	}
}
