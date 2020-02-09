package kr.co.shineware.nlp.komoran.core.model.combinationrules;

import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.core.model.MorphUtil;
import kr.co.shineware.nlp.komoran.core.model.TagUtil;

public class VerbEomiCombinationRuleChecker implements CombinationRuleChecker {

    private final MorphUtil morphUtil;
    private final TagUtil tagUtil;

    public VerbEomiCombinationRuleChecker(MorphUtil morphUtil, TagUtil tagUtil) {
        this.morphUtil = morphUtil;
        this.tagUtil = tagUtil;
    }

    @Override
    public boolean isValidRule(String prevMorph, int prevTagId, String morph, int tagId) {

        //이전이 동사인 경우
        if (this.tagUtil.getId(SYMBOL.VV) == prevTagId) {
            boolean hasJongsung = this.morphUtil.hasJongsung(prevMorph);

            //관형형 전성어미
            if (this.tagUtil.getId(SYMBOL.ETM) == tagId) {
                if (hasJongsung) {
                    return this.morphUtil.isSameJaso(morph, "을")
                            || this.morphUtil.isSameJaso(morph, "은");
                } else {
                    return this.morphUtil.isSameJaso(morph, "ㄹ")
                            || this.morphUtil.isSameJaso(morph, "ㄴ");
                }
            }
            //명사형 전성어미
            else if (this.tagUtil.getId(SYMBOL.ETN) == tagId) {
                if (hasJongsung) {
                    return this.morphUtil.isSameJaso(morph, "음");
                } else {
                    return this.morphUtil.isSameJaso(morph, "ㅁ");
                }
            }
        }

        return true;
    }
}
