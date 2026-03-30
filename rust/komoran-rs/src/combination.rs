/// 조합 규칙 검증 모듈
/// MergedCombinationRuleChecker.java 포팅

use crate::model::TagUtil;
use crate::constant::SejongTags;
use crate::jaso;

pub struct CombinationRuleChecker {
    tag_util: TagUtil,
    sejong_tags: SejongTags,
}

impl CombinationRuleChecker {
    pub fn new(tag_util: TagUtil, sejong_tags: SejongTags) -> Self {
        CombinationRuleChecker { tag_util, sejong_tags }
    }

    pub fn is_valid_rule(&self, prev_morph: &str, prev_tag_id: i32, morph: &str, tag_id: i32) -> bool {
        // 이전이 명사류인 경우
        if self.tag_util.is_noun(prev_tag_id) {
            if self.tag_util.is_eomi(tag_id) {
                return false; // 명사 + 어미 금지
            }
            if self.tag_util.is_josa(tag_id) {
                return self.check_noun_josa_rule(prev_morph, morph, tag_id);
            }
        }
        // 이전이 동사(VV)인 경우
        else if self.sejong_tags.vv_id == prev_tag_id {
            return self.check_verb_eomi_rule(prev_morph, morph, tag_id);
        }
        true
    }

    fn check_verb_eomi_rule(&self, prev_morph: &str, morph: &str, tag_id: i32) -> bool {
        // 관형형 전성어미
        if self.sejong_tags.etm_id == tag_id {
            if morph == "ㅇㅡㄹ" || morph == "ㅇㅡㄴ" {
                return jaso::has_jongsung(prev_morph);
            } else if morph == "ㄹ" || morph == "ㄴ" {
                return !jaso::has_jongsung(prev_morph);
            }
        }
        // 명사형 전성어미
        else if self.sejong_tags.etn_id == tag_id {
            if morph == "ㅇㅡㅁ" {
                return jaso::has_jongsung(prev_morph);
            } else if morph == "ㅁ" {
                return !jaso::has_jongsung(prev_morph);
            }
        }
        true
    }

    fn check_noun_josa_rule(&self, prev_morph: &str, morph: &str, tag_id: i32) -> bool {
        let tags = &self.sejong_tags;
        let has_jong = jaso::has_jongsung(prev_morph);

        // 주격조사
        if tags.jks_id == tag_id {
            if morph == "ㅇㅣ" { return has_jong; }
            if morph == "ㄱㅣ" { return !has_jong; }
        }
        // 보격조사
        else if tags.jkc_id == tag_id {
            if morph == "ㅇㅣ" { return has_jong; }
            if morph == "ㄱㅏ" { return !has_jong; }
        }
        // 목적격조사
        else if tags.jko_id == tag_id {
            if morph == "ㅇㅡㄹ" { return has_jong; }
            if morph == "ㄹ" || morph == "ㄹㅡㄹ" { return !has_jong; }
        }
        // 호격조사
        else if tags.jkv_id == tag_id {
            if morph == "ㅇㅏ" { return has_jong; }
            if morph == "ㅇㅑ" { return !has_jong; }
        }
        // 접속조사
        else if tags.jc_id == tag_id {
            if morph == "ㄱㅘ" || morph == "ㅇㅣㄴㅏ" || morph == "ㅇㅣㄹㅏㅇ" {
                return has_jong;
            }
            if morph == "ㅇㅘ" || morph == "ㄴㅏ" || morph == "ㄹㅏㅇ" {
                return !has_jong;
            }
        }
        // 부사격조사
        else if tags.jkb_id == tag_id {
            if morph == "ㄱㅘ" || morph == "ㅇㅡㄹㅗ" { return has_jong; }
            if morph == "ㅇㅘ" || morph == "ㄹㅗ" { return !has_jong; }
        }
        // 보조사
        else if tags.jx_id == tag_id {
            if morph == "ㅇㅡㄴ" || morph == "ㅇㅣㄹㅏㄴ" { return has_jong; }
            if morph == "ㄴㅡㄴ" || morph == "ㄹㅏㄴ" { return !has_jong; }
        }
        true
    }
}
