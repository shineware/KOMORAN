/// KOMORAN 메인 분석기
/// 최적화 v3: tag_id-only LatticeNode + 재사용 검색 버퍼

use pyo3::prelude::*;
use std::sync::Arc;

use crate::jaso::{self, UnitType};
use crate::lattice::{Lattice, LatticeNode};
use crate::model::Resources;
use crate::combination::CombinationRuleChecker;
use crate::constant;
use crate::trie::{AhoCorasickTrie, ScoredTag, IrregularNode};

/// 분석 결과 토큰
#[pyclass]
#[derive(Clone, Debug)]
pub struct PyToken {
    #[pyo3(get)]
    pub morph: String,
    #[pyo3(get)]
    pub pos: String,
    #[pyo3(get)]
    pub begin_index: usize,
    #[pyo3(get)]
    pub end_index: usize,
}

#[pymethods]
impl PyToken {
    fn __repr__(&self) -> String {
        format!("{}/{}", self.morph, self.pos)
    }

    fn __str__(&self) -> String {
        format!("{}/{}", self.morph, self.pos)
    }
}

/// 출력용 노드 (tag 문자열 포함 — 출력 시에만 해석)
pub struct OutputNode {
    pub begin_idx: i32,
    pub end_idx: i32,
    pub morph: String,
    pub tag: String,
    pub tag_id: i32,
}

/// 분석 결과
pub struct KomoranResult {
    pub nodes: Vec<OutputNode>,
    pub jaso_units: String,
}

impl KomoranResult {
    pub fn get_plain_text(&self) -> String {
        let mut parts = Vec::new();
        for node in &self.nodes {
            if node.tag == constant::EOE { continue; }
            if node.tag == constant::NA {
                parts.push(format!("{}/{}", node.morph, node.tag));
            } else {
                parts.push(format!("{}/{}", jaso::combine(&node.morph), node.tag));
            }
        }
        parts.join(" ")
    }

    pub fn get_token_list(&self) -> Vec<PyToken> {
        let syllable_areas = jaso::get_syllable_area_list(&self.jaso_units);
        let max_jaso_idx = self.jaso_units.chars().count();

        // Java와 동일한 O(1) 맵 구성 (last match wins at boundaries)
        let mut begin_map = vec![-1i32; max_jaso_idx + 1];
        let mut end_map = vec![-1i32; max_jaso_idx + 1];

        for (i, &(first, second)) in syllable_areas.iter().enumerate() {
            let limit = second.min(max_jaso_idx);
            for j in first..=limit {
                begin_map[j] = i as i32;
            }
            if second <= max_jaso_idx {
                end_map[second] = i as i32 + 1;
            }
        }

        let mut tokens = Vec::new();
        let mut prev_begin_idx = 0i32;

        for node in &self.nodes {
            if node.tag == constant::EOE { continue; }

            let jaso_begin = if node.begin_idx < 0 { prev_begin_idx } else { node.begin_idx };
            let jaso_end = node.end_idx;

            let jb = jaso_begin as usize;
            let je = jaso_end as usize;

            let syllable_begin = if jb < begin_map.len() && begin_map[jb] >= 0 {
                begin_map[jb] as usize
            } else {
                find_syllable_begin(jb, &syllable_areas)
            };

            let syllable_end = if je < end_map.len() && end_map[je] >= 0 {
                end_map[je] as usize
            } else {
                find_syllable_end(je, &syllable_areas)
            };

            tokens.push(PyToken {
                morph: jaso::combine(&node.morph),
                pos: node.tag.clone(),
                begin_index: syllable_begin,
                end_index: syllable_end,
            });

            prev_begin_idx = jaso_begin;
        }
        tokens
    }

    pub fn get_nouns(&self) -> Vec<String> {
        self.get_morphs_by_tags(&[constant::NNG, constant::NNP])
    }

    pub fn get_morphs_by_tags(&self, tags: &[&str]) -> Vec<String> {
        self.nodes.iter()
            .filter(|n| tags.contains(&n.tag.as_str()))
            .map(|n| jaso::combine(&n.morph))
            .collect()
    }

    pub fn get_list(&self) -> Vec<(String, String)> {
        self.nodes.iter()
            .filter(|n| n.tag != constant::EOE)
            .map(|n| (jaso::combine(&n.morph), n.tag.clone()))
            .collect()
    }
}

fn find_syllable_begin(jaso_idx: usize, areas: &[(usize, usize)]) -> usize {
    for (i, &(first, second)) in areas.iter().enumerate() {
        if first <= jaso_idx && jaso_idx <= second {
            return i;
        }
    }
    0
}

fn find_syllable_end(jaso_idx: usize, areas: &[(usize, usize)]) -> usize {
    for (i, &(first, second)) in areas.iter().enumerate() {
        if first < jaso_idx && jaso_idx <= second {
            return i + 1;
        }
    }
    0
}

/// tag_id → tag 문자열 변환 (출력 시에만 호출)
#[inline]
fn resolve_tag(tag_id: i32, resources: &Resources) -> String {
    if tag_id == constant::IRREGULAR_ID {
        return constant::IRREGULAR.to_string();
    }
    resources.pos_table.get_pos(tag_id)
        .unwrap_or("NA")
        .to_string()
}

/// LatticeNode → OutputNode 변환 (경로 노드에만 적용)
fn to_output_nodes(path: &[LatticeNode], resources: &Resources) -> Vec<OutputNode> {
    path.iter().map(|n| OutputNode {
        begin_idx: n.begin_idx,
        end_idx: n.end_idx,
        morph: n.morph.clone(),
        tag: resolve_tag(n.tag_id, resources),
        tag_id: n.tag_id,
    }).collect()
}

/// KOMORAN 분석기
pub struct KomoranEngine {
    resources: Resources,
    rule_checker: CombinationRuleChecker,
    user_dic: Option<AhoCorasickTrie<Vec<ScoredTag>>>,
    fwd: Option<std::collections::HashMap<String, Vec<(String, String)>>>,
}

impl KomoranEngine {
    pub fn new(model_path: &str) -> Self {
        let resources = Resources::load(model_path);
        let rule_checker = CombinationRuleChecker::new(
            resources.tag_util.clone(),
            resources.sejong_tags.clone(),
        );
        KomoranEngine {
            resources,
            rule_checker,
            user_dic: None,
            fwd: None,
        }
    }

    pub fn analyze(&self, sentence: &str, nbest: usize) -> Vec<KomoranResult> {
        if sentence.is_empty() {
            return vec![KomoranResult { nodes: vec![], jaso_units: String::new() }];
        }

        let jaso_units_with_type = jaso::parse_with_type(sentence);
        let jaso_units: String = jaso_units_with_type.iter().map(|&(ch, _)| ch).collect();
        let jaso_chars: Vec<char> = jaso_units.chars().collect();
        let length = jaso_chars.len();

        let mut lattice = Lattice::new(
            &self.resources.transition,
            &self.resources.pos_table,
            &self.resources.sejong_tags,
            &self.rule_checker,
            nbest,
        );

        // Trie 컨텍스트 + 재사용 검색 버퍼
        let mut obs_context = self.resources.observation.new_find_context();
        let mut irr_context = self.resources.irregular.new_find_context();
        let mut user_dic_context = self.user_dic.as_ref().map(|d| d.new_find_context());

        let mut obs_results: Vec<(&str, &Vec<ScoredTag>, usize)> = Vec::new();
        let mut irr_results: Vec<(&str, &Vec<IrregularNode>, usize)> = Vec::new();
        let mut user_results: Vec<(&str, &Vec<ScoredTag>, usize)> = Vec::new();

        let mut whitespace_index: i32 = 0;
        let mut prev_pos = String::new();
        let mut prev_morph = String::new();
        let mut prev_begin_idx: i32 = 0;

        let mut cur_idx: usize = 0;
        while cur_idx < length {
            let jaso = jaso_chars[cur_idx];
            let idx = cur_idx as i32;

            if let Some(skip) = self.lookup_fwd(&mut lattice, &jaso_units, cur_idx) {
                cur_idx = skip;
                continue;
            }

            if jaso == ' ' {
                self.consume_continuous_symbol(&mut lattice, idx, &mut prev_pos, &mut prev_morph, &mut prev_begin_idx);
                self.bridge_token(&mut lattice, idx, &jaso_units_with_type, whitespace_index);
                whitespace_index = idx + 1;
            }

            self.continuous_symbol_parsing(&mut lattice, jaso, idx, &mut prev_pos, &mut prev_morph, &mut prev_begin_idx);
            self.symbol_parsing(&mut lattice, jaso, idx);

            // 사용자 사전
            if let (Some(dic), Some(ref mut ctx)) = (&self.user_dic, &mut user_dic_context) {
                dic.search_into(ctx, jaso, &mut user_results);
                let end_idx = idx + 1;
                for &(morph, scored_tags, char_count) in &user_results {
                    let begin_idx = idx - char_count as i32 + 1;
                    for st in scored_tags {
                        lattice.put(begin_idx, end_idx, morph, st.tag_id, st.score);
                    }
                }
            }

            // 정규 파싱
            self.resources.observation.search_into(&mut obs_context, jaso, &mut obs_results);
            {
                let end_idx = idx + 1;
                for &(morph, scored_tags, char_count) in &obs_results {
                    let begin_idx = idx - char_count as i32 + 1;
                    for st in scored_tags {
                        lattice.put(begin_idx, end_idx, morph, st.tag_id, st.score);
                        if st.tag_id == self.resources.sejong_tags.ec_id {
                            lattice.put(begin_idx, end_idx, morph, self.resources.sejong_tags.ef_id, st.score);
                        }
                    }
                }
            }

            // 불규칙 파싱
            self.resources.irregular.search_into(&mut irr_context, jaso, &mut irr_results);
            {
                for &(_morph, irr_nodes, char_count) in &irr_results {
                    let begin_idx = idx - char_count as i32 + 1;
                    let end_idx = idx + 1;
                    for irr_node in irr_nodes {
                        lattice.put_irregular(begin_idx, end_idx, irr_node, &self.resources.observation);
                    }
                }
            }

            // 불규칙 확장
            self.irregular_extends(&mut lattice, jaso, idx);

            cur_idx += 1;
        }

        self.consume_continuous_symbol(&mut lattice, length as i32, &mut prev_pos, &mut prev_morph, &mut prev_begin_idx);

        lattice.set_last_idx(length as i32);
        let inserted = lattice.append_end_node();

        if !inserted {
            let na_score = constant::SCORE_NA
                + if whitespace_index != 0 {
                    lattice.get_node_list(whitespace_index).map(|n| n[0].score).unwrap_or(0.0)
                } else { 0.0 };

            let combined = jaso::combine_with_type(&jaso_units_with_type[whitespace_index as usize..]);
            let mut na_node = LatticeNode::new(whitespace_index, length as i32, combined, self.resources.sejong_tags.na_id, na_score);
            na_node.prev_node_idx = 0;
            lattice.append_node(na_node);
            lattice.append_end_node();
        }

        let nbest_paths = lattice.find_nbest_path();

        match nbest_paths {
            None => {
                let node = OutputNode {
                    begin_idx: 0,
                    end_idx: length as i32,
                    morph: sentence.to_string(),
                    tag: "NA".to_string(),
                    tag_id: -1,
                };
                vec![KomoranResult { nodes: vec![node], jaso_units }]
            }
            Some(paths) => {
                paths.into_iter().map(|mut path| {
                    path.reverse();
                    let output_nodes = to_output_nodes(&path, &self.resources);
                    KomoranResult { nodes: output_nodes, jaso_units: jaso_units.clone() }
                }).collect()
            }
        }
    }

    fn lookup_fwd(&self, lattice: &mut Lattice, jaso_units: &str, cur_idx: usize) -> Option<usize> {
        let fwd = self.fwd.as_ref()?;
        let chars: Vec<char> = jaso_units.chars().collect();
        if cur_idx != 0 && chars[cur_idx - 1] != ' ' { return None; }

        let word_end = jaso_units[cur_idx..].find(' ')
            .map(|p| cur_idx + p)
            .unwrap_or(chars.len());
        let target: String = chars[cur_idx..word_end].iter().collect();

        if let Some(fwd_list) = fwd.get(&target) {
            lattice.put_fwd(cur_idx as i32, word_end as i32, fwd_list);
            Some(word_end)
        } else {
            None
        }
    }

    fn bridge_token(&self, lattice: &mut Lattice, cur_idx: i32, jaso_with_type: &[(char, UnitType)], prev_begin: i32) {
        if lattice.put(cur_idx, cur_idx + 1, constant::EOE, self.resources.sejong_tags.eoe_id, 0.0) {
            return;
        }
        let combined = jaso::combine_with_type(&jaso_with_type[prev_begin as usize..cur_idx as usize]);
        let na_id = self.resources.pos_table.get_id(constant::NA);
        let mut na_node = LatticeNode::new(prev_begin, cur_idx, combined, na_id, constant::SCORE_NA);
        na_node.prev_node_idx = 0;
        let na_idx = lattice.append_node(na_node) as i32;

        let mut end_node = LatticeNode::new(cur_idx, cur_idx + 1, constant::EOE.to_string(), self.resources.sejong_tags.eoe_id, 0.0);
        end_node.prev_node_idx = na_idx;
        lattice.append_node(end_node);
    }

    fn symbol_parsing(&self, lattice: &mut Lattice, jaso: char, idx: i32) {
        if jaso.is_ascii_digit() { return; }

        if jaso.is_ascii() {
            if !is_english(jaso) && jaso != ' ' && self.resources.observation.get_value(&jaso.to_string()).is_none() {
                lattice.put(idx, idx + 1, &jaso.to_string(), self.resources.sejong_tags.sw_id, constant::SCORE_SW);
            }
        } else if !is_korean(jaso) && !is_japanese(jaso) && !is_chinese(jaso) {
            lattice.put(idx, idx + 1, &jaso.to_string(), self.resources.sejong_tags.sw_id, constant::SCORE_SW);
        }
    }

    fn irregular_extends(&self, lattice: &mut Lattice, jaso: char, cur_idx: i32) {
        let idx = cur_idx;

        let irr_data: Vec<(String, i32, i32, f64)> = match lattice.get_node_list(idx) {
            Some(nodes) => nodes.iter()
                .filter(|n| n.tag_id == constant::IRREGULAR_ID)
                .map(|n| (n.morph.clone(), n.begin_idx, n.prev_node_idx, n.score))
                .collect(),
            None => return,
        };

        if irr_data.is_empty() { return; }

        let mut extended_nodes = Vec::new();

        for (last_morph, begin_idx, prev_node_idx, score) in &irr_data {
            let mut morph_with_jaso: Vec<char> = last_morph.chars().collect();
            morph_with_jaso.push(jaso);

            if self.resources.observation.has_child(&morph_with_jaso) {
                let morph_str: String = morph_with_jaso.iter().collect();
                let mut ext_node = LatticeNode::new(
                    *begin_idx, cur_idx + 1,
                    morph_str, constant::IRREGULAR_ID, *score,
                );
                ext_node.prev_node_idx = *prev_node_idx;
                extended_nodes.push(ext_node);
            }

            let morph_str: String = morph_with_jaso.iter().collect();
            if let Some(scored_tags) = self.resources.observation.get_value(&morph_str) {
                for st in scored_tags {
                    lattice.put(*begin_idx, cur_idx + 1, &morph_str, st.tag_id, st.score);
                }
            }
        }

        for node in extended_nodes {
            lattice.append_node(node);
        }
    }

    fn continuous_symbol_parsing(&self, lattice: &mut Lattice, jaso: char, idx: i32,
                                  prev_pos: &mut String, prev_morph: &mut String, prev_begin_idx: &mut i32) {
        let cur_pos = if is_english(jaso) || is_foreign(jaso) {
            "SL"
        } else if jaso.is_ascii_digit() {
            "SN"
        } else if is_chinese(jaso) {
            "SH"
        } else {
            ""
        };

        if cur_pos == prev_pos.as_str() && !cur_pos.is_empty() {
            prev_morph.push(jaso);
        } else {
            if !prev_pos.is_empty() {
                let score = get_score_for_pos(prev_pos);
                if score != f64::NEG_INFINITY {
                    let tag_id = self.resources.pos_table.get_id(prev_pos);
                    lattice.put(*prev_begin_idx, idx, prev_morph, tag_id, score);
                }
            }
            *prev_begin_idx = idx;
            *prev_morph = jaso.to_string();
            *prev_pos = cur_pos.to_string();
        }
    }

    fn consume_continuous_symbol(&self, lattice: &mut Lattice, end_idx: i32,
                                  prev_pos: &mut String, prev_morph: &mut String, prev_begin_idx: &mut i32) {
        if prev_pos.is_empty() { return; }
        let score = get_score_for_pos(prev_pos);
        if score != f64::NEG_INFINITY {
            let tag_id = self.resources.pos_table.get_id(prev_pos);
            lattice.put(*prev_begin_idx, end_idx, prev_morph, tag_id, score);
        }
        *prev_pos = String::new();
        *prev_morph = String::new();
    }
}

fn get_score_for_pos(pos: &str) -> f64 {
    match pos {
        "SL" => constant::SCORE_SL,
        "SN" => constant::SCORE_SN,
        "SH" => constant::SCORE_SH,
        _ => f64::NEG_INFINITY,
    }
}

fn is_english(ch: char) -> bool { ch.is_ascii_alphabetic() }
fn is_korean(ch: char) -> bool {
    let c = ch as u32;
    (0xAC00..=0xD7A3).contains(&c) || (0x3131..=0x3163).contains(&c)
}
fn is_japanese(ch: char) -> bool {
    let c = ch as u32;
    (0x3040..=0x309F).contains(&c) || (0x30A0..=0x30FF).contains(&c)
}
fn is_chinese(ch: char) -> bool {
    let c = ch as u32;
    (0x4E00..=0x9FFF).contains(&c) || (0x3400..=0x4DBF).contains(&c)
}
fn is_foreign(ch: char) -> bool {
    let c = ch as u32;
    (0x30A0..=0x30FF).contains(&c) || (0xFF00..=0xFFEF).contains(&c)
}

// ============ PyO3 Python 바인딩 ============

#[pyclass]
pub struct PyKomoran {
    engine: Arc<KomoranEngine>,
}

#[pymethods]
impl PyKomoran {
    #[new]
    #[pyo3(signature = (model_path))]
    fn new(model_path: &str) -> PyResult<Self> {
        let engine = KomoranEngine::new(model_path);
        Ok(PyKomoran { engine: Arc::new(engine) })
    }

    #[pyo3(signature = (sentence))]
    fn analyze(&self, sentence: &str) -> String {
        let results = self.engine.analyze(sentence, 1);
        results.first().map(|r| r.get_plain_text()).unwrap_or_default()
    }

    #[pyo3(signature = (sentence))]
    fn tokens(&self, sentence: &str) -> Vec<PyToken> {
        let results = self.engine.analyze(sentence, 1);
        results.first().map(|r| r.get_token_list()).unwrap_or_default()
    }

    #[pyo3(signature = (sentence))]
    fn nouns(&self, sentence: &str) -> Vec<String> {
        let results = self.engine.analyze(sentence, 1);
        results.first().map(|r| r.get_nouns()).unwrap_or_default()
    }

    #[pyo3(signature = (sentence))]
    fn pos(&self, sentence: &str) -> Vec<(String, String)> {
        let results = self.engine.analyze(sentence, 1);
        results.first().map(|r| r.get_list()).unwrap_or_default()
    }

    #[pyo3(signature = (sentences))]
    fn analyze_batch(&self, sentences: Vec<String>) -> Vec<String> {
        use rayon::prelude::*;
        let engine = &self.engine;
        sentences.par_iter()
            .map(|s| {
                let results = engine.analyze(s, 1);
                results.first().map(|r| r.get_plain_text()).unwrap_or_default()
            })
            .collect()
    }

    #[pyo3(signature = (sentences))]
    fn tokens_batch(&self, sentences: Vec<String>) -> Vec<Vec<PyToken>> {
        use rayon::prelude::*;
        let engine = &self.engine;
        sentences.par_iter()
            .map(|s| {
                let results = engine.analyze(s, 1);
                results.first().map(|r| r.get_token_list()).unwrap_or_default()
            })
            .collect()
    }

    fn __repr__(&self) -> String {
        "Komoran(model='loaded')".to_string()
    }
}
