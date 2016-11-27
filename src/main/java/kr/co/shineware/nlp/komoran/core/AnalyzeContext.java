package kr.co.shineware.nlp.komoran.core;

import kr.co.shineware.ds.aho_corasick.FindContext;
import kr.co.shineware.nlp.komoran.core.model.Lattice;
import kr.co.shineware.nlp.komoran.core.model.Resources;
import kr.co.shineware.nlp.komoran.model.ScoredTag;
import kr.co.shineware.nlp.komoran.modeler.model.IrregularNode;
import kr.co.shineware.nlp.komoran.modeler.model.IrregularTrie;
import kr.co.shineware.nlp.komoran.modeler.model.Observation;
import kr.co.shineware.nlp.komoran.modeler.model.PosTable;
import kr.co.shineware.nlp.komoran.parser.KoreanUnitParser;

import java.util.List;
import java.util.Map;

public class AnalyzeContext {
	private final Lattice lattice;
	private final FindContext<List<ScoredTag>> observFindCtx;
	private final FindContext<List<ScoredTag>> userDicFindCtx;
	private final FindContext<List<IrregularNode>> irrFindCtx;

	private final IrregularTrie irregularTrie;
	private final Observation observation;
	private final Observation userDic;
	private final PosTable posTable;

	//연속된 숫자, 외래어, 기호 등을 파싱 하기 위한 버퍼
	private String prevPos;
	private String prevMorph;
	private int prevBeginIdx;

	public AnalyzeContext(final Resources resources, final KoreanUnitParser parser, final Observation userDic) {
		this.lattice = new Lattice(resources);
		this.lattice.setUnitParser(parser);

		this.observation = resources.getObservation();
		this.irregularTrie = resources.getIrrTrie();
		this.posTable = resources.getTable();
		this.userDic = userDic;

		this.observFindCtx = observation.getTrieDictionary().newFindContext();
		this.irrFindCtx = irregularTrie.getTrieDictionary().newFindContext();
		if (userDic != null) {
			this.userDicFindCtx = userDic.getTrieDictionary().newFindContext();
		} else {
			this.userDicFindCtx = null;
		}

		this.prevPos = "";
		this.prevMorph = "";
		this.prevBeginIdx = 0;
	}

	public Lattice getLattice() {
		return lattice;
	}

	public void updatePrevPos(final String prevPos) {
		this.prevPos = prevPos;
	}

	public String getPrevPos() {
		return prevPos;
	}

	public boolean isPrevPos(final String pos) {
		return this.prevPos.equals(pos);
	}

	public boolean isPrevPosEmpty() {
		return this.prevPos.trim().length() == 0;
	}

	public void appendPrevMorph(final char ch) {
		this.prevMorph += ch;
	}

	public void updatePrevMorph(final String prevMorph) {
		this.prevMorph = prevMorph;
	}

	public String getPrevMorph() {
		return prevMorph;
	}

	public void updatePrevBeginIdx(final int idx) {
		this.prevBeginIdx = idx;
	}

	public int getPrevBeginIdx() {
		return prevBeginIdx;
	}

	public void putToLattice(final int endIdx, final double score) {
		lattice.put(prevBeginIdx, endIdx, prevMorph, prevPos, posTable.getId(prevPos), score);
	}

	public Map<String, List<IrregularNode>> getIrregularNodes(final char jaso) {
		return irregularTrie.getTrieDictionary().get(irrFindCtx, jaso);
	}

	public Map<String, List<ScoredTag>> getMorphScoredTagsMap(final char jaso) {
		return observation.getTrieDictionary().get(observFindCtx, jaso);
	}

	public Map<String, List<ScoredTag>> getMorphScoredTagMapFromUserDic(final char jaso) {
		if (this.userDic == null) {
			return null;
		}
		return this.userDic.getTrieDictionary().get(userDicFindCtx, jaso);
	}
}
