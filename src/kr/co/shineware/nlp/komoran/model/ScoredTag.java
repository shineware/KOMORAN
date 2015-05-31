package kr.co.shineware.nlp.komoran.model;

public class ScoredTag extends Tag{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double score;
	
	public ScoredTag(String tag, int tagId, double score){
		super(tag,tagId);
		this.setScore(score);
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public String toString() {
		return "ScoredTag [score=" + score + ", getTagId()=" + getTagId()
				+ ", getTag()=" + getTag() + "]";
	}
}
