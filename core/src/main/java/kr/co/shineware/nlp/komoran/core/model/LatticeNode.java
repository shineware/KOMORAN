/*******************************************************************************
 * KOMORAN 3.0 - Korean Morphology Analyzer
 *
 * Copyright 2015 Shineware http://www.shineware.co.kr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package kr.co.shineware.nlp.komoran.core.model;

import kr.co.shineware.nlp.komoran.model.MorphTag;

public class LatticeNode {

    private int beginIdx;
    private int endIdx;
    // 핫 패스용 인라인 필드 (MorphTag 포인터 체이싱 제거)
    private String morph;
    private String tag;
    private int tagId;
    private double score;
    private int prevNodeIdx = -1;

    public LatticeNode() {
    }

    public LatticeNode(int beginIdx, int endIdx, MorphTag morphTag, double score) {
        this.beginIdx = beginIdx;
        this.endIdx = endIdx;
        this.morph = morphTag.getMorph();
        this.tag = morphTag.getTag();
        this.tagId = morphTag.getTagId();
        this.score = score;
    }

    public LatticeNode(int beginIdx, int endIdx, String morph, String tag, int tagId, double score) {
        this.beginIdx = beginIdx;
        this.endIdx = endIdx;
        this.morph = morph;
        this.tag = tag;
        this.tagId = tagId;
        this.score = score;
    }

    public LatticeNode(LatticeNode other) {
        this.beginIdx = other.beginIdx;
        this.endIdx = other.endIdx;
        this.morph = other.morph;
        this.tag = other.tag;
        this.tagId = other.tagId;
        this.score = other.score;
        this.prevNodeIdx = other.prevNodeIdx;
    }

    /** 풀링: 기존 노드 객체의 필드를 재설정하여 재사용 */
    public void reset(int beginIdx, int endIdx, String morph, String tag, int tagId, double score, int prevNodeIdx) {
        this.beginIdx = beginIdx;
        this.endIdx = endIdx;
        this.morph = morph;
        this.tag = tag;
        this.tagId = tagId;
        this.score = score;
        this.prevNodeIdx = prevNodeIdx;
    }

    public int getBeginIdx() { return beginIdx; }
    public void setBeginIdx(int beginIdx) { this.beginIdx = beginIdx; }

    public int getEndIdx() { return endIdx; }
    public void setEndIdx(int endIdx) { this.endIdx = endIdx; }

    public String getMorph() { return morph; }
    public void setMorph(String morph) { this.morph = morph; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public int getTagId() { return tagId; }
    public void setTagId(int tagId) { this.tagId = tagId; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public int getPrevNodeIdx() { return prevNodeIdx; }
    public void setPrevNodeIdx(int prevNodeIdx) { this.prevNodeIdx = prevNodeIdx; }

    /** 하위 호환용. MorphTag를 필요 시 생성하여 반환. */
    public MorphTag getMorphTag() {
        return new MorphTag(morph, tag, tagId);
    }

    public void setMorphTag(MorphTag morphTag) {
        this.morph = morphTag.getMorph();
        this.tag = morphTag.getTag();
        this.tagId = morphTag.getTagId();
    }

    @Override
    public String toString() {
        return "LatticeNode [beginIdx=" + beginIdx + ", endIdx=" + endIdx
                + ", morph=" + morph + ", tag=" + tag + ", tagId=" + tagId
                + ", score=" + score + ", prevNodeIdx=" + prevNodeIdx + "]";
    }
}
