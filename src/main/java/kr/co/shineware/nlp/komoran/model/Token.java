package kr.co.shineware.nlp.komoran.model;

import java.util.Objects;

public class Token {
	private String morph;
	private String pos;
	private int beginIndex;
	private int endIndex;

	public Token(){
    }

	public Token(String morph, String pos, int beginIndex, int endIndex) {
		super();
		this.setMorph(morph);
		this.setBeginIndex(beginIndex);
		this.setEndIndex(endIndex);
		this.setPos(pos);
	}
	
	public String getMorph() {
		return morph;
	}
	public void setMorph(String morph) {
		this.morph = morph;
	}
	public String getPos() {
		return pos;
	}
	public void setPos(String pos) {
		this.pos = pos;
	}
	public int getBeginIndex() {
		return beginIndex;
	}
	public void setBeginIndex(int beginIndex) {
		this.beginIndex = beginIndex;
	}
	public int getEndIndex() {
		return endIndex;
	}
	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}
	@Override
	public String toString() {
		return "Token [morph=" + morph + ", pos=" + pos + ", beginIndex="
				+ beginIndex + ", endIndex=" + endIndex + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Token token = (Token) o;
		return Objects.equals(morph, token.morph) &&
				Objects.equals(pos, token.pos);
	}

	@Override
	public int hashCode() {
		return Objects.hash(morph, pos);
	}
}
