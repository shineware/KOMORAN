package kr.co.shineware.nlp.komoran.core.model.combinationrules;

import kr.co.shineware.nlp.komoran.constant.SYMBOL;
import kr.co.shineware.nlp.komoran.core.model.MorphUtil;
import kr.co.shineware.nlp.komoran.core.model.TagUtil;

public class NounJosaCombinationRuleChecker implements CombinationRuleChecker {

    private final MorphUtil morphUtil;
    private final TagUtil tagUtil;

    public NounJosaCombinationRuleChecker(MorphUtil morphUtil, TagUtil tagUtil) {
        this.morphUtil = morphUtil;
        this.tagUtil = tagUtil;
    }

    @Override
    public boolean isValidRule(String prevMorph, int prevTagId, String morph, int tagId) {
        //이전이 명사류이고 현재가 조사인경우
        if (this.tagUtil.isNoun(prevTagId) && this.tagUtil.isJosa(tagId)) {

            boolean hasJongsung = this.morphUtil.hasJongsung(prevMorph);

            //주격조사
            if (this.tagUtil.getId(SYMBOL.JKS) == tagId) {
                if (hasJongsung) {
                    return this.morphUtil.isSameJaso(morph, "이");
                } else {
                    return this.morphUtil.isSameJaso(morph, "기");
                }
            }
            //보격조사
            else if (this.tagUtil.getId(SYMBOL.JKC) == tagId) {
                if (hasJongsung) {
                    return this.morphUtil.isSameJaso(morph, "이");
                } else {
                    return this.morphUtil.isSameJaso(morph, "가");
                }
            }
            //목적격조사
            else if (this.tagUtil.getId(SYMBOL.JKO) == tagId) {
                if (hasJongsung) {
                    return this.morphUtil.isSameJaso(morph, "을");
                } else {
                    return this.morphUtil.isSameJaso(morph, "ㄹ")
                            || this.morphUtil.isSameJaso(morph, "를");
                }
            }
            //호격조사
            else if (this.tagUtil.getId(SYMBOL.JKV) == tagId) {
                if (hasJongsung) {
                    return this.morphUtil.isSameJaso(morph, "아");
                } else {
                    return this.morphUtil.isSameJaso(morph, "야");
                }
            }
            //접속조사
            else if (this.tagUtil.getId(SYMBOL.JC) == tagId) {
                if (hasJongsung) {
                    return this.morphUtil.isSameJaso(morph, "과")
                            || this.morphUtil.isSameJaso(morph, "이나")
                            || this.morphUtil.isSameJaso(morph, "이랑");
                } else {
                    return this.morphUtil.isSameJaso(morph, "와")
                            || this.morphUtil.isSameJaso(morph, "나")
                            || this.morphUtil.isSameJaso(morph, "랑");
                }
            }
            //부사격조사
            else if (this.tagUtil.getId(SYMBOL.JKB) == tagId) {
                if (hasJongsung) {
                    return this.morphUtil.isSameJaso(morph, "과")
                            || this.morphUtil.isSameJaso(morph, "으로");
                } else {
                    return this.morphUtil.isSameJaso(morph, "와")
                            || this.morphUtil.isSameJaso(morph, "로");
                }
            }
            //보조사
            else if (this.tagUtil.getId(SYMBOL.JX) == tagId) {
                if (hasJongsung) {
                    return this.morphUtil.isSameJaso(morph, "은")
                            || this.morphUtil.isSameJaso(morph, "이란");
                } else {
                    return this.morphUtil.isSameJaso(morph, "는")
                            || this.morphUtil.isSameJaso(morph, "란");
                }
            }
        }
        return true;
    }
}
