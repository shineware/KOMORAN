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


    @Override
    public String toString() {
        return "LatticeNode [beginIdx=" + beginIdx + ", endIdx=" + endIdx
                + ", morphTag=" + morphTag + ", score=" + score
                + ", prevNodeIdx=" + prevNodeIdx + ", morph=" + morph + "]";
    }

    private int beginIdx;
    private int endIdx;
    private MorphTag morphTag;
    private double score;
    private int prevNodeIdx = -1;
    private String morph;

    public LatticeNode() {
    }

    public LatticeNode(int beginIdx, int endIdx, MorphTag morphTag, double score) {
        this.beginIdx = beginIdx;
        this.endIdx = endIdx;
        this.morphTag = morphTag;
        this.score = score;
    }

    public LatticeNode(LatticeNode latticeNode) {
        this.beginIdx = latticeNode.getBeginIdx();
        this.endIdx = latticeNode.getEndIdx();
        this.morphTag = latticeNode.getMorphTag();
        this.score = latticeNode.getScore();
    }

    public int getBeginIdx() {
        return beginIdx;
    }

    public void setBeginIdx(int beginIdx) {
        this.beginIdx = beginIdx;
    }

    public int getEndIdx() {
        return endIdx;
    }

    public void setEndIdx(int endIdx) {
        this.endIdx = endIdx;
    }

    public MorphTag getMorphTag() {
        return morphTag;
    }

    public void setMorphTag(MorphTag morphTag) {
        this.morphTag = morphTag;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getPrevNodeIdx() {
        return prevNodeIdx;
    }

    public void setPrevNodeIdx(int prevNodeIdx) {
        this.prevNodeIdx = prevNodeIdx;
    }

    public String getTag() {
        return this.morphTag.getTag();
    }
}
