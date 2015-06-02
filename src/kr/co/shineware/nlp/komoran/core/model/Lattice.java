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
	
	
	public Lattice(){
		this.init();
	}
	
	private void init() {
		this.lattice = new HashMap<Integer, List<LatticeNode>>();
		List<LatticeNode> latticeNodes = new ArrayList<>();
		latticeNodes.add(this.makeStartNode());
		this.lattice.put(0, latticeNodes);
	}

	private LatticeNode makeStartNode() {
		return new LatticeNode(-1,0,new MorphTag(SYMBOL.START, SYMBOL.START,getPosTable().getId(SYMBOL.START)),0);
	}

	public void put(int beginIdx, int endIdx, String morph, Tag tag,
			double score) {
		List<LatticeNode> prevLatticeNodes = this.lattice.get(beginIdx);
		if(prevLatticeNodes == null){
			;
		}else{
			int maxTransitionIdx = this.getMaxTransitionIdx(prevLatticeNodes,tag);
			
			if(maxTransitionIdx != -1){
				this.appendNode(beginIdx,endIdx,morph,tag,score);
			}
		}
	}

	private void appendNode(int beginIdx, int endIdx, String morph, Tag tag,
			double score) {
		// TODO Auto-generated method stub
		
	}

	private int getMaxTransitionIdx(List<LatticeNode> prevLatticeNodes, Tag tag) {
		
		int prevMaxIdx = -1;
		double maxScore = Double.NEGATIVE_INFINITY;
		
		for(int i=0; i<prevLatticeNodes.size(); i++){
			Double transitionScore = this.transition.get(prevLatticeNodes.get(i).getMorphTag().getTagId(), tag.getTagId());
			if(transitionScore == null){
				continue;
			}
			if(maxScore < transitionScore){
				maxScore = transitionScore;
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
}
