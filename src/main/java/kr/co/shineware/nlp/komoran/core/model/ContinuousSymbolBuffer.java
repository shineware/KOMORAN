package kr.co.shineware.nlp.komoran.core.model;

/**
 * Created by shin285 on 2017. 1. 23..
 */
public class ContinuousSymbolBuffer {
	private String prevPos = "";
	private String prevMorph = "";
	private int prevBeginIdx = 0;

	public String getPrevPos() {
		return prevPos;
	}

	public void setPrevPos(String prevPos) {
		this.prevPos = prevPos;
	}

	public String getPrevMorph() {
		return prevMorph;
	}

	public void setPrevMorph(String prevMorph) {
		this.prevMorph = prevMorph;
	}

	public int getPrevBeginIdx() {
		return prevBeginIdx;
	}

	public void setPrevBeginIdx(int prevBeginIdx) {
		this.prevBeginIdx = prevBeginIdx;
	}
}
