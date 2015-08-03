package kr.co.shineware.nlp.komoran.model;

public class ScoredTag extends Tag{

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(score);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScoredTag other = (ScoredTag) obj;
		if (Double.doubleToLongBits(score) != Double
				.doubleToLongBits(other.score))
			return false;
		return true;
	}

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
