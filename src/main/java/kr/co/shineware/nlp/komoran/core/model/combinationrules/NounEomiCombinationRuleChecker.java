package kr.co.shineware.nlp.komoran.core.model.combinationrules;

import kr.co.shineware.nlp.komoran.core.model.TagUtil;

public class NounEomiCombinationRuleChecker implements CombinationRuleChecker {

    private final TagUtil tagUtil;

    public NounEomiCombinationRuleChecker(TagUtil tagUtil) {
        this.tagUtil = tagUtil;
    }

    @Override
    public boolean isValidRule(String prevMorph, int prevTagId, String morph, int tagId) {
        if (this.tagUtil.isNoun(prevTagId) && this.tagUtil.isEomi(tagId)) {
            return false;
        }
        return true;
    }
}
