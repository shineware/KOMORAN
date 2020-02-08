package kr.co.shineware.nlp.komoran.core.model.combinationrules;

public interface CombinationRuleChecker {
    boolean isValidRule(String prevMorph, int prevTagId, String morph, int tagId);
}
