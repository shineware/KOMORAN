package kr.co.shineware.nlp.komoran.model;

public class MorphScoredTag extends ScoredTag{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String morph;
	
	public MorphScoredTag(String tag, int tagId, double score, String morph) {
		super(tag, tagId, score);
		this.morph = morph;
	}
	public String getMorph() {
		return morph;
	}
	public void setMorph(String morph) {
		this.morph = morph;
	}
	
}
