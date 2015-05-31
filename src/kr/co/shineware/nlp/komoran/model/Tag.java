package kr.co.shineware.nlp.komoran.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Tag extends ArrayList<String> implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int tagId;
	private String tag;
	public Tag(String tag, int tagId) {
		this.setTag(tag);
		this.setTagId(tagId);
	}
	public int getTagId() {
		return tagId;
	}
	public void setTagId(int tagId) {
		this.tagId = tagId;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	@Override
	public String toString() {
		return "Tag [tagId=" + tagId + ", tag=" + tag + "]";
	}
}
