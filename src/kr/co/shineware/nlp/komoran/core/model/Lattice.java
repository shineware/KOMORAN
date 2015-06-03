package kr.co.shineware.nlp.komoran.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.model.MorphTag;
import kr.co.shineware.nlp.komoran.model.Tag;
import kr.co.shineware.nlp.komoran.modeler.model.PosTable;
import kr.co.shineware.nlp.komoran.modeler.model.Transition;

public class Lattice {
	
	private Map<Integer, List<LatticeNode>> lattice;
//	private Map<Integer, List<IrregularLatticeNode>> irregularLattice;
	private PosTable posTable;
	private Transition transition;
	private int lastIdx = -1;
	
	public Lattice(Resources resource) {
		this.setPosTable(resource.getTable());
		this.setTransition(resource.getTransition());
		this.init();
	}

	private void init() {
		this.lattice = new HashMap<Integer, List<LatticeNode>>();
		List<LatticeNode> latticeNodes = new ArrayList<>();
		latticeNodes.add(this.makeStartNode());
		this.lattice.put(0, latticeNodes);
	}

	private LatticeNode makeStartNode() {
		return new LatticeNode(-1,0,new MorphTag(SYMBOL.START, SYMBOL.START,this.getPosTable().getId(SYMBOL.START)),0);
	}

	public void put(int beginIdx, int endIdx, String morph, Tag tag, double score) {
		List<LatticeNode> prevLatticeNodes = this.getNodeList(beginIdx);
		if(prevLatticeNodes == null){
			;
		}else{
			int maxTransitionPrevIdx = this.getMaxTransitionIdxFromPrevNodes(prevLatticeNodes,tag);
			
			if(maxTransitionPrevIdx != -1){
				LatticeNode maxPrevLatticeNode = prevLatticeNodes.get(maxTransitionPrevIdx);
				double transitionScore = this.getTransitionScore(maxPrevLatticeNode.getMorphTag().getTagId(), tag.getTagId());
				double prevNodeScore = maxPrevLatticeNode.getScore();
				LatticeNode latticeNode = this.makeNode(beginIdx,endIdx,morph,tag.getTag(),tag.getTagId(),prevNodeScore+transitionScore+score,maxTransitionPrevIdx);
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

	private double getTransitionScore(int prevTagId, int curTagId) {
		return this.transition.get(prevTagId, curTagId);
	}

	private void appendNode(LatticeNode latticeNode) {
		List<LatticeNode> latticeNodes = this.getNodeList(latticeNode.getEndIdx());
		if(latticeNodes == null){
			latticeNodes = new ArrayList<LatticeNode>();
		}
		latticeNodes.add(latticeNode);
		this.lattice.put(latticeNode.getEndIdx(), latticeNodes);
	}
	private List<LatticeNode> getNodeList(int index){
		return this.lattice.get(index);
	}

	private int getMaxTransitionIdxFromPrevNodes(List<LatticeNode> prevLatticeNodes, Tag tag) {
		
		int prevMaxIdx = -1;
		double maxScore = Double.NEGATIVE_INFINITY;
		
		for(int i=0; i<prevLatticeNodes.size(); i++){
			Double transitionScore = this.transition.get(prevLatticeNodes.get(i).getMorphTag().getTagId(), tag.getTagId());
			if(transitionScore == null){
				continue;
			}
			
			double prevObservationScore = prevLatticeNodes.get(i).getScore();
			
			if(maxScore < transitionScore+prevObservationScore){
				maxScore = transitionScore+prevObservationScore;
				prevMaxIdx = i;
			}
		}
		return prevMaxIdx;
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
		for(int i=0;i<this.getLastIdx()+2;i++){
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
		this.put(this.lastIdx, this.lastIdx+1, SYMBOL.END, new Tag(SYMBOL.END, this.getPosTable().getId(SYMBOL.END)), 0);
	}

	public void findPath() {
		int idx = this.getLastIdx()+1;
		LatticeNode latticeNode = this.lattice.get(idx).get(0);
		while(true){
			latticeNode = this.lattice.get(latticeNode.getBeginIdx()).get(latticeNode.getPrevNodeIdx());
			System.out.println(latticeNode.getMorphTag());
			if(latticeNode.getBeginIdx() == -1){
				break;
			}
		}
	}
}
