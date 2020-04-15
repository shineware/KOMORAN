package kr.co.shineware.nlp.komoran.core.model.combinationrules;

import kr.co.shineware.nlp.komoran.constant.SEJONGTAGS;
import kr.co.shineware.nlp.komoran.core.model.MorphUtil;

public class VerbEomiCombinationRuleChecker implements CombinationRuleChecker {

    private final MorphUtil morphUtil;

    public VerbEomiCombinationRuleChecker(MorphUtil morphUtil) {
        this.morphUtil = morphUtil;
    }

    @Override
    public boolean isValidRule(String prevMorph, int prevTagId, String morph, int tagId) {
        //이전이 동사인 경우
        if (SEJONGTAGS.VV_ID == prevTagId) {

            //관형형 전성어미
            if (SEJONGTAGS.ETM_ID == tagId) {
                boolean hasJongsung = this.morphUtil.hasJongsung(prevMorph);
                if (hasJongsung) {
                    return this.morphUtil.isSameJaso(morph, "ㅇㅡㄹ")
                            || this.morphUtil.isSameJaso(morph, "ㅇㅡㄴ");
                } else {
                    return this.morphUtil.isSameJaso(morph, "ㄹ")
                            || this.morphUtil.isSameJaso(morph, "ㄴ");
                }
            }
            //명사형 전성어미
            else if (SEJONGTAGS.ETN_ID == tagId) {
                boolean hasJongsung = this.morphUtil.hasJongsung(prevMorph);
                if (hasJongsung) {
                    return this.morphUtil.isSameJaso(morph, "ㅇㅡㅁ");
                } else {
                    return this.morphUtil.isSameJaso(morph, "ㅁ");
                }
            }
        }

        return true;
    }
}
