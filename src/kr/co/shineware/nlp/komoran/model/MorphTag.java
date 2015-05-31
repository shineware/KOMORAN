package kr.co.shineware.nlp.komoran.model;

public class MorphTag extends Tag{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String morph;
	
	public MorphTag(String morph, String tag, int tagId){
		super(tag, tagId);
	}

	public String getMorph() {
		return morph;
	}

	public void setMorph(String morph) {
		this.morph = morph;
	}

	@Override
	public String toString() {
		return "MorphTag [morph=" + morph + ", getTagId()=" + getTagId()
				+ ", getTag()=" + getTag() + "]";
	}
	
	
	
}
