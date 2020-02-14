package kr.co.shineware.nlp.komoran.core.model.combinationrules;

import kr.co.shineware.nlp.komoran.constant.SEJONGTAGS;
import kr.co.shineware.nlp.komoran.core.model.MorphUtil;
import kr.co.shineware.nlp.komoran.core.model.TagUtil;

public class MergedCombinationRuleChecker implements CombinationRuleChecker {

    private final MorphUtil morphUtil;
    private final TagUtil tagUtil;

    public MergedCombinationRuleChecker(MorphUtil morphUtil, TagUtil tagUtil) {
        this.morphUtil = morphUtil;
        this.tagUtil = tagUtil;
    }

    @Override
    public boolean isValidRule(String prevMorph, int prevTagId, String morph, int tagId) {

        //이전이 명사류이고 현재가 조사인경우
        if (this.tagUtil.isNoun(prevTagId)) {
            if (this.tagUtil.isEomi(tagId)) {
                return false;

            }
            //현재가 조사인경우
            else if (this.tagUtil.isJosa(tagId)) {
                return chekcNounWithJongsungAndJosaRule(prevMorph, morph, tagId);
            }
        }

        //이전이 동사인 경우
        else if (SEJONGTAGS.VV_ID == prevTagId) {
            //관형형 전성어미 VV+ETN, VV+ETM
            return checktNounWithJongsungAndEomiRul(prevMorph, morph, tagId);
        }

        return true;
    }

    private boolean checktNounWithJongsungAndEomiRul(String prevMorph, String morph, int tagId) {
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
        return true;
    }

    private boolean chekcNounWithJongsungAndJosaRule(String prevMorph, String morph, int tagId) {
        //주격조사
        if (SEJONGTAGS.JKS_ID == tagId) {
            boolean hasJongsung = this.morphUtil.hasJongsung(prevMorph);
            if (hasJongsung) {
                return this.morphUtil.isSameJaso(morph, "ㅇㅣ");
            } else {
                return this.morphUtil.isSameJaso(morph, "ㄱㅣ");
            }
        }
        //보격조사
        else if (SEJONGTAGS.JKC_ID == tagId) {
            boolean hasJongsung = this.morphUtil.hasJongsung(prevMorph);
            if (hasJongsung) {
                return this.morphUtil.isSameJaso(morph, "ㅇㅣ");
            } else {
                return this.morphUtil.isSameJaso(morph, "ㄱㅏ");
            }
        }
        //목적격조사
        else if (SEJONGTAGS.JKO_ID == tagId) {
            boolean hasJongsung = this.morphUtil.hasJongsung(prevMorph);
            if (hasJongsung) {
                return this.morphUtil.isSameJaso(morph, "ㅇㅡㄹ");
            } else {
                return this.morphUtil.isSameJaso(morph, "ㄹ")
                        || this.morphUtil.isSameJaso(morph, "ㄹㅡㄹ");
            }
        }
        //호격조사
        else if (SEJONGTAGS.JKV_ID == tagId) {
            boolean hasJongsung = this.morphUtil.hasJongsung(prevMorph);
            if (hasJongsung) {
                return this.morphUtil.isSameJaso(morph, "ㅇㅏ");
            } else {
                return this.morphUtil.isSameJaso(morph, "ㅇㅑ");
            }
        }
        //접속조사
        else if (SEJONGTAGS.JC_ID == tagId) {
            boolean hasJongsung = this.morphUtil.hasJongsung(prevMorph);
            if (hasJongsung) {
                return this.morphUtil.isSameJaso(morph, "ㄱㅘ")
                        || this.morphUtil.isSameJaso(morph, "ㅇㅣㄴㅏ")
                        || this.morphUtil.isSameJaso(morph, "ㅇㅣㄹㅏㅇ");
            } else {
                return this.morphUtil.isSameJaso(morph, "ㅇㅘ")
                        || this.morphUtil.isSameJaso(morph, "ㄴㅏ")
                        || this.morphUtil.isSameJaso(morph, "ㄹㅏㅇ");
            }
        }
        //부사격조사
        else if (SEJONGTAGS.JKB_ID == tagId) {
            boolean hasJongsung = this.morphUtil.hasJongsung(prevMorph);
            if (hasJongsung) {
                return this.morphUtil.isSameJaso(morph, "ㄱㅘ")
                        || this.morphUtil.isSameJaso(morph, "ㅇㅡㄹㅗ");
            } else {
                return this.morphUtil.isSameJaso(morph, "ㅇㅘ")
                        || this.morphUtil.isSameJaso(morph, "ㄹㅗ");
            }
        }
        //보조사
        else if (SEJONGTAGS.JX_ID == tagId) {
            boolean hasJongsung = this.morphUtil.hasJongsung(prevMorph);
            if (hasJongsung) {
                return this.morphUtil.isSameJaso(morph, "ㅇㅡㄴ")
                        || this.morphUtil.isSameJaso(morph, "ㅇㅣㄹㅏㄴ");
            } else {
                return this.morphUtil.isSameJaso(morph, "ㄴㅡㄴ")
                        || this.morphUtil.isSameJaso(morph, "ㄹㅏㄴ");
            }
        }
        return true;
    }
}
