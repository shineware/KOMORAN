package kr.co.shineware.nlp.komoran.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.model.MorphTag;
import kr.co.shineware.nlp.komoran.model.ScoredTag;
import kr.co.shineware.nlp.komoran.modeler.model.IrregularNode;
import kr.co.shineware.nlp.komoran.modeler.model.Observation;
import kr.co.shineware.nlp.komoran.modeler.model.PosTable;
import kr.co.shineware.nlp.komoran.modeler.model.Transition;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;
import kr.co.shineware.util.common.model.Pair;

public class Lattice {

	private static final int IRREGULAR_POS_ID = -1;
	private Map<Integer, List<LatticeNode>> lattice;
	private PosTable posTable;
	private Transition transition;
	private int lastIdx = -1;
	private int irrIdx = 0;
	private Observation observation;

	private KoreanUnitParser unitParser;

	private double prevMaxScore;
	private LatticeNode prevMaxNode;
	private int prevMaxIdx;

	public Lattice(Resources resource) {
		this.setPosTable(resource.getTable());
		this.setTransition(resource.getTransition());
		this.setObservation(resource.getObservation());
		this.init();
	}

	private void init() {

		this.lattice = new HashMap<Integer, List<LatticeNode>>();
		irrIdx = 0;

		List<LatticeNode> latticeNodes = new ArrayList<>();
		latticeNodes.add(this.makeStartNode());

		this.lattice.put(0, latticeNodes);
	}

	private LatticeNode makeStartNode() {
		return new LatticeNode(-1,0,new MorphTag(SYMBOL.START, SYMBOL.START,this.getPosTable().getId(SYMBOL.START)),0);
	}

	public void put(int beginIdx, int endIdx, IrregularNode irregularNode) {
		//현재 node를 연결 시킬 이전 node list들을 가져옴
		List<LatticeNode> prevLatticeNodes = this.getNodeList(beginIdx);
		if(prevLatticeNodes == null){
			;
		}else{
			this.prevMaxIdx = -1;
			this.prevMaxNode = null;
			this.prevMaxScore = Double.NEGATIVE_INFINITY;
			this.getMaxTransitionIdxFromPrevNodes(prevLatticeNodes,irregularNode.getFirstPosId());
			if(this.prevMaxNode != null){
				List<Pair<String,Integer>> irregularTokens = irregularNode.getTokens();
				//불규칙확장을 위한 노드 추가
				int prevMaxIdx = this.prevMaxIdx;
				double prevMaxScore = this.prevMaxScore;
				this.putIrregularExtendTokens(beginIdx, endIdx, irregularTokens,prevMaxScore, prevMaxIdx);

				
				//일반 불규칙을 노드를 추가하기 위한 루틴
				this.putFirstIrrgularNode(beginIdx, endIdx, irregularTokens, prevMaxScore, prevMaxIdx);
				this.putIrregularTokens(beginIdx, endIdx, irregularTokens);
			}
		}
	}

	private void putIrregularExtendTokens(int beginIdx, int endIdx,
			List<Pair<String, Integer>> morphPosIdList, double prevMaxScore, int prevMaxIdx) {

		//첫번쨰 토큰에 대한 처리
		if(morphPosIdList.size() == 0){
			LatticeNode firstNode = this.makeNode(beginIdx, endIdx, morphPosIdList.get(0).getFirst(), "IRR", IRREGULAR_POS_ID, prevMaxScore, prevMaxIdx);
			this.appendNode(firstNode);
		}
		else{
			Pair<String,Integer> morphPosId = morphPosIdList.get(0);
			List<ScoredTag> scoredTags = this.observation.getTrieDictionary().getValue(morphPosId.getFirst());
			for (ScoredTag scoredTag : scoredTags) {
				if(scoredTag.getTagId() == morphPosId.getSecond()){
					LatticeNode firstIrregularNode = this.makeNode(beginIdx, irrIdx-1, morphPosId.getFirst(), scoredTag.getTag(), scoredTag.getTagId(), prevMaxScore+scoredTag.getScore(), prevMaxIdx);
					irrIdx--;
					this.appendNode(firstIrregularNode);
				}
			}
		}

		for(int i=1;i<morphPosIdList.size();i++){
			Pair<String,Integer> morphPosId = morphPosIdList.get(i);
			//마지막 토큰에 대해서는 IRR 태그를 넣어줌 이때 score는 0.0을 줌
			if(i == morphPosIdList.size()-1){
				//					this.put(irrIdx, endIdx, morphPosId.getFirst(), "IRR", morphPosId.getSecond(), 0.0);
//				LatticeNode latticeNode = new LatticeNode(irrIdx, endIdx,new MorphTag(morphPosId.getFirst(),"IRR", IRREGULAR_POS_ID),0.0);
				LatticeNode latticeNode = this.makeNode(irrIdx, endIdx, morphPosId.getFirst(), "IRR", IRREGULAR_POS_ID, 0.0, 0);
				this.appendNode(latticeNode);

			}
			else{
				List<ScoredTag> scoredTags = this.observation.getTrieDictionary().getValue(morphPosId.getFirst());
				for (ScoredTag scoredTag : scoredTags) {
					if(scoredTag.getTagId() == morphPosId.getSecond()){
						this.put(irrIdx, irrIdx-1, morphPosId.getFirst(), this.posTable.getPos(morphPosId.getSecond()), morphPosId.getSecond(), scoredTag.getScore());
					}
				}
			}
			irrIdx--;
		}
	}

	private void putFirstIrrgularNode(int beginIdx, int endIdx,
			List<Pair<String, Integer>> irregularTokens, double score,
			int maxTransitionPrevIdx) {
		if(irregularTokens.size() == 1){
			Pair<String,Integer> morphPosId = irregularTokens.get(0);
			List<ScoredTag> scoredTags = this.observation.getTrieDictionary().getValue(morphPosId.getFirst());
			for (ScoredTag scoredTag : scoredTags) {
				if(scoredTag.getTagId() == morphPosId.getSecond()){
					LatticeNode firstIrregularNode = this.makeNode(beginIdx, endIdx, morphPosId.getFirst(), scoredTag.getTag(), scoredTag.getTagId(), score+scoredTag.getScore(), maxTransitionPrevIdx);
					this.appendNode(firstIrregularNode);
				}
			}
		}else{
			Pair<String,Integer> morphPosId = irregularTokens.get(0);
			List<ScoredTag> scoredTags = this.observation.getTrieDictionary().getValue(morphPosId.getFirst());
			for (ScoredTag scoredTag : scoredTags) {
				if(scoredTag.getTagId() == morphPosId.getSecond()){
					LatticeNode firstIrregularNode = this.makeNode(beginIdx, irrIdx-1, morphPosId.getFirst(), scoredTag.getTag(), scoredTag.getTagId(), score+scoredTag.getScore(), maxTransitionPrevIdx);
					irrIdx--;
					this.appendNode(firstIrregularNode);
				}
			}

		}
	}

	//	public void put(int beginIdx, int endIdx, String morph, Tag tag, double score) {
	public void put(int beginIdx, int endIdx, String morph, String tag, int tagId, double score) {
		List<LatticeNode> prevLatticeNodes = this.getNodeList(beginIdx);
		if(prevLatticeNodes == null){
			;
		}else{
			this.prevMaxIdx = -1;
			this.prevMaxNode = null;
			//prevMaxScore는 이전 노드까지의 누적된 score와 현재 노드 사이의 전이확률 값이 계산된 결과임
			this.prevMaxScore = Double.NEGATIVE_INFINITY;
			//			이전 node list에 포함된 node 중 현재 node와 연결 시 값을 최대로 하는 node의 인덱스를 찾음
			this.getMaxTransitionInfoFromPrevNodes(prevLatticeNodes,tagId,morph);

			//연결 가능한 노드가 있다면
			if(this.prevMaxNode != null){
				LatticeNode latticeNode = this.makeNode(beginIdx,endIdx,morph,tag,tagId,this.prevMaxScore+score,this.prevMaxIdx);
				this.appendNode(latticeNode);

			}

		}
	}

	private LatticeNode makeNode(int beginIdx, int endIdx, String morph,
			String tag, int tagId, double score, int prevLatticeIdx) {
		LatticeNode latticeNode = new LatticeNode(beginIdx, endIdx, new MorphTag(morph, tag, tagId), score);
		latticeNode.setPrevNodeIdx(prevLatticeIdx);
		return latticeNode;
	}

	public void appendNode(LatticeNode latticeNode) {
		List<LatticeNode> latticeNodeList = this.getNodeList(latticeNode.getEndIdx());
		if(latticeNodeList == null){
			latticeNodeList = new ArrayList<>();
		}
		latticeNodeList.add(latticeNode);
		this.lattice.put(latticeNode.getEndIdx(), latticeNodeList);
	}
	public List<LatticeNode> getNodeList(int index){
		return this.lattice.get(index);
	}

	private void getMaxTransitionIdxFromPrevNodes(List<LatticeNode> prevLatticeNodeSet, int tagId) {
		this.getMaxTransitionInfoFromPrevNodes(prevLatticeNodeSet, tagId, null);
	}

	private void getMaxTransitionInfoFromPrevNodes(List<LatticeNode> prevLatticeNodeSet, int tagId, String morph) {

		for(int i=0; i<prevLatticeNodeSet.size(); i++){

			//불규칙인경우
			if(prevLatticeNodeSet.get(i).getMorphTag().getTagId() == -1){
				continue;
			}
			//전이 확률 값 가져옴
			Double transitionScore = this.transition.get(prevLatticeNodeSet.get(i).getMorphTag().getTagId(), tagId);
			if(transitionScore == null){
				continue;
			}

			//자소 결합규칙 체크
			if(tagId == this.posTable.getId(SYMBOL.JKO)){
				if(this.hasJongsung(prevLatticeNodeSet.get(i).getMorphTag().getMorph())){
					if(morph.charAt(0) != 'ㅇ'){
						continue;
					}
				}else{
					if(morph.charAt(0) == 'ㅇ'){
						continue;
					}
				}
			}else if(tagId == this.posTable.getId(SYMBOL.JKS)
					|| tagId == this.posTable.getId(SYMBOL.JKC)){
				if(this.hasJongsung(prevLatticeNodeSet.get(i).getMorphTag().getMorph())){
					if(morph.equals("ㄱㅏ")){
						continue;
					}
				}else{
					if(morph.equals("ㅇㅣ")){
						continue;
					}
				}
			}

			double prevObservationScore = prevLatticeNodeSet.get(i).getScore();

			if(this.prevMaxScore < transitionScore+prevObservationScore){
				this.prevMaxScore = transitionScore+prevObservationScore;
				this.prevMaxNode = prevLatticeNodeSet.get(i);
				this.prevMaxIdx = i;
			}
		}
	}

	private boolean hasJongsung(String str) {
		char prevLastJaso = str.charAt(str.length()-1);
		if(0x3131 <= prevLastJaso && prevLastJaso <= 0x314e){
			if(prevLastJaso == 0x3138 || prevLastJaso ==  0x3143 || prevLastJaso == 0x3149){
				return false;
			}else{
				return true;
			}
		}
		return false;
	}

	public PosTable getPosTable() {
		return posTable;
	}

	public void setPosTable(PosTable posTable) {
		this.posTable = posTable;
	}

	public Transition getTransition() {
		return transition;
	}

	public void setTransition(Transition transition) {
		this.transition = transition;
	}

	public void printLattice() {
		for(int i=irrIdx;i<this.getLastIdx()+2;i++){
			System.out.println("["+i+"]");
			List<LatticeNode> nodeList = this.lattice.get(i);
			if(nodeList == null){
				continue;
			}
			for (LatticeNode latticeNode : nodeList) {
				System.out.println(latticeNode);
			}
			System.out.println();
		}
	}

	public int getLastIdx() {
		return lastIdx;
	}

	public void setLastIdx(int lastIdx) {
		this.lastIdx = lastIdx;
	}

	public void appendEndNode() {
		this.put(this.lastIdx, this.lastIdx+1, SYMBOL.END, SYMBOL.END, this.getPosTable().getId(SYMBOL.END), 0);
	}

	public List<Pair<String,String>> findPath() {
		List<Pair<String,String>> shortestPathList = new ArrayList<>();
		int idx = this.getLastIdx()+1;
		//마지막 연결 노드가 없는 경우에는 null 반환
		if(this.lattice.containsKey(idx) == false){
			return null;
		}

		LatticeNode latticeNode = this.lattice.get(idx).get(0);

		while(true){
			latticeNode = this.lattice.get(latticeNode.getBeginIdx()).get(latticeNode.getPrevNodeIdx());
			List<Pair<String,String>> irregularNode = this.splitIrregularNode(latticeNode);
			if(irregularNode == null){
				shortestPathList.add(new Pair<>(this.unitParser.combine(latticeNode.getMorphTag().getMorph()),latticeNode.getMorphTag().getTag()));
			}else{
				shortestPathList.addAll(irregularNode);
			}

			if(latticeNode.getBeginIdx() == 0){
				break;
			}
		}
		return shortestPathList;
	}

	private List<Pair<String, String>> splitIrregularNode(
			LatticeNode latticeNode) {
		String[] tokens = latticeNode.getMorphTag().getMorph().split(" ");
		if(tokens.length == 1){
			return null;
		}else{
			List<Pair<String,String>> irregularNodeList = new ArrayList<>();
			for(int i=tokens.length-1;i>=0;i--){
				String token = tokens[i];
				int delimeterIdx = token.lastIndexOf('/');
				if(delimeterIdx == -1){
					irregularNodeList.add(new Pair<>(this.unitParser.combine(token),latticeNode.getMorphTag().getTag()));
					continue;
				}
				String morph = token.substring(0, delimeterIdx);
				String pos = token.substring(delimeterIdx+1);
				irregularNodeList.add(new Pair<>(this.unitParser.combine(morph),pos));
			}
			return irregularNodeList;
		}
	}

	private void putIrregularTokens(int beginIdx, int endIdx, List<Pair<String,Integer>> morphPosIdList){

		for(int i=1; i<morphPosIdList.size(); i++){
			Pair<String,Integer> morphPosId = morphPosIdList.get(i);
			List<ScoredTag> scoredTags = this.observation.getTrieDictionary().getValue(morphPosId.getFirst());
			if(i == morphPosIdList.size()-1){
				for (ScoredTag scoredTag : scoredTags) {
					if(scoredTag.getTagId() == morphPosId.getSecond()){
						this.put(irrIdx, endIdx, morphPosId.getFirst(), this.posTable.getPos(morphPosId.getSecond()), morphPosId.getSecond(), scoredTag.getScore());
					}
				}				
			} else{
				for (ScoredTag scoredTag : scoredTags) {
					if(scoredTag.getTagId() == morphPosId.getSecond()){
						this.put(irrIdx, irrIdx-1, morphPosId.getFirst(), this.posTable.getPos(morphPosId.getSecond()), morphPosId.getSecond(), scoredTag.getScore());
					}
				}
			}
			irrIdx--;
		}
	}

	public void put(int beginIdx, int endIdx, String rawFormat) {
		String[] tokens = rawFormat.split(" ");

		for(int i=0;i<tokens.length;i++){
			String token = tokens[i];
			String morph = token.split("\\/")[0];
			String pos = token.split("\\/")[1];
			if(i == 0){
				List<ScoredTag> scoredTags = this.observation.getTrieDictionary().getValue(morph);
				for (ScoredTag scoredTag : scoredTags) {
					if(scoredTag.getTag().equals(pos)){
						this.put(beginIdx, irrIdx-1, morph, scoredTag.getTag(), scoredTag.getTagId(), scoredTag.getScore());
					}
				}

			} else if(i == tokens.length-1){
				List<ScoredTag> scoredTags = this.observation.getTrieDictionary().getValue(morph);
				for (ScoredTag scoredTag : scoredTags) {
					if(scoredTag.getTag().equals(pos)){
						this.put(irrIdx, endIdx, morph, pos, this.posTable.getId(pos), scoredTag.getScore());
					}
				}				
			} else{
				List<ScoredTag> scoredTags = this.observation.getTrieDictionary().getValue(morph);
				for (ScoredTag scoredTag : scoredTags) {
					if(scoredTag.getTag().equals(pos)){
						this.put(irrIdx, irrIdx-1, morph, pos, this.posTable.getId(pos), scoredTag.getScore());
					}
				}
			}
			irrIdx--;
		}
	}

	public Observation getObservation() {
		return observation;
	}

	public void setObservation(Observation observation) {
		this.observation = observation;
	}

	public KoreanUnitParser getUnitParser() {
		return unitParser;
	}

	public void setUnitParser(KoreanUnitParser unitParser) {
		this.unitParser = unitParser;
	}
}
