/// Lattice + Viterbi 모듈
/// 최적화 v3: LatticeNode에서 tag String 제거 → tag_id만 저장

use std::collections::HashMap;

use crate::trie::{ScoredTag, IrregularNode, AhoCorasickTrie};
use crate::model::{Transition, PosTable};
use crate::combination::CombinationRuleChecker;
use crate::constant::{self, SejongTags};

/// 격자 노드 — tag String 제거로 힙 할당 감소
#[derive(Clone, Debug)]
pub struct LatticeNode {
    pub begin_idx: i32,
    pub end_idx: i32,
    pub morph: String,
    pub tag_id: i32,
    pub score: f64,
    pub prev_node_idx: i32,
}

impl LatticeNode {
    #[inline]
    pub fn new(begin_idx: i32, end_idx: i32, morph: String, tag_id: i32, score: f64) -> Self {
        LatticeNode { begin_idx, end_idx, morph, tag_id, score, prev_node_idx: -1 }
    }
}

/// Lattice 구조
pub struct Lattice<'a> {
    positive_nodes: Vec<Option<Vec<LatticeNode>>>,
    negative_nodes: HashMap<i32, Vec<LatticeNode>>,

    transition: &'a Transition,
    pos_table: &'a PosTable,
    sejong_tags: &'a SejongTags,
    rule_checker: &'a CombinationRuleChecker,

    last_idx: i32,
    pub irr_idx: i32,
    nbest: usize,
}

impl<'a> Lattice<'a> {
    pub fn new(
        transition: &'a Transition,
        pos_table: &'a PosTable,
        sejong_tags: &'a SejongTags,
        rule_checker: &'a CombinationRuleChecker,
        nbest: usize,
    ) -> Self {
        let mut positive_nodes = Vec::with_capacity(256);
        positive_nodes.resize_with(256, || None);

        let mut lattice = Lattice {
            positive_nodes,
            negative_nodes: HashMap::new(),
            transition,
            pos_table,
            sejong_tags,
            rule_checker,
            last_idx: -1,
            irr_idx: 0,
            nbest,
        };

        let start_node = LatticeNode::new(-1, 0, constant::BOE.to_string(), sejong_tags.boe_id, 0.0);
        lattice.put_node_list(0, vec![start_node]);
        lattice
    }

    fn ensure_capacity(&mut self, index: usize) {
        if index >= self.positive_nodes.len() {
            self.positive_nodes.resize_with(index + 128, || None);
        }
    }

    fn put_node_list(&mut self, index: i32, nodes: Vec<LatticeNode>) {
        if index >= 0 {
            let idx = index as usize;
            self.ensure_capacity(idx);
            self.positive_nodes[idx] = Some(nodes);
        } else {
            self.negative_nodes.insert(index, nodes);
        }
    }

    pub fn get_node_list(&self, index: i32) -> Option<&Vec<LatticeNode>> {
        if index >= 0 {
            let idx = index as usize;
            if idx < self.positive_nodes.len() {
                self.positive_nodes[idx].as_ref()
            } else {
                None
            }
        } else {
            self.negative_nodes.get(&index)
        }
    }

    pub fn set_last_idx(&mut self, idx: i32) {
        self.last_idx = idx;
    }

    /// 노드 추가 (tag 문자열 없이 tag_id만 사용)
    pub fn put(&mut self, begin_idx: i32, end_idx: i32, morph: &str, tag_id: i32, score: f64) -> bool {
        let result = {
            let prev_nodes = match self.get_node_list(begin_idx) {
                Some(nodes) => nodes,
                None => return false,
            };

            if self.nbest > 1 {
                self.get_nbest_max_transition(prev_nodes, begin_idx, end_idx, morph, tag_id, score)
            } else {
                self.get_max_transition(prev_nodes, begin_idx, end_idx, morph, tag_id, score)
                    .map(|n| vec![n])
            }
        };

        if let Some(nodes) = result {
            for node in nodes {
                self.append_node(node);
            }
            true
        } else {
            false
        }
    }

    /// 불규칙 노드 처리
    pub fn put_irregular(&mut self, begin_idx: i32, end_idx: i32, irr_node: &IrregularNode, observation: &AhoCorasickTrie<Vec<ScoredTag>>) {
        let (prev_max_score, prev_max_idx) = {
            let prev_nodes = match self.get_node_list(begin_idx) {
                Some(nodes) => nodes,
                None => return,
            };

            let col_scores = self.transition.get_column_scores(irr_node.first_pos_id);
            let mut prev_max_score = f64::NEG_INFINITY;
            let mut prev_max_idx: i32 = -1;

            for (i, prev_node) in prev_nodes.iter().enumerate() {
                let prev_tag_id = prev_node.tag_id;
                if prev_tag_id == -1 { continue; }
                let actual_prev_id = if prev_tag_id == self.sejong_tags.eoe_id {
                    self.sejong_tags.boe_id
                } else {
                    prev_tag_id
                };
                if actual_prev_id < 0 || actual_prev_id as usize >= col_scores.len() { continue; }
                let trans_score = col_scores[actual_prev_id as usize];
                if trans_score == f64::NEG_INFINITY { continue; }
                let total = trans_score + prev_node.score;
                if total > prev_max_score {
                    prev_max_score = total;
                    prev_max_idx = i as i32;
                }
            }

            (prev_max_score, prev_max_idx)
        };

        if prev_max_idx == -1 { return; }

        let tokens = &irr_node.tokens;
        if tokens.is_empty() { return; }

        if tokens.len() == 1 {
            let (ref morph, pos_id) = tokens[0];
            if let Some(scored_tags) = observation.get_value(morph) {
                for st in scored_tags {
                    if st.tag_id == pos_id {
                        let mut node = LatticeNode::new(begin_idx, end_idx, morph.clone(), st.tag_id, prev_max_score + st.score);
                        node.prev_node_idx = prev_max_idx;
                        self.append_node(node);
                        if st.tag_id == self.sejong_tags.ec_id {
                            let mut ef_node = LatticeNode::new(begin_idx, end_idx, morph.clone(), self.sejong_tags.ef_id, prev_max_score + st.score);
                            ef_node.prev_node_idx = prev_max_idx;
                            self.append_node(ef_node);
                        }
                    }
                }
            }
            return;
        }

        // 다중 토큰 불규칙 — 첫 번째 토큰
        let (ref first_morph, first_pos_id) = tokens[0];
        if let Some(scored_tags) = observation.get_value(first_morph) {
            for st in scored_tags {
                if st.tag_id == first_pos_id {
                    let mut node = LatticeNode::new(begin_idx, self.irr_idx - 1, first_morph.clone(), st.tag_id, prev_max_score + st.score);
                    node.prev_node_idx = prev_max_idx;
                    self.irr_idx -= 1;
                    self.append_node(node);
                }
            }
        }

        // 나머지 토큰
        for i in 1..tokens.len() {
            let (ref morph, pos_id) = tokens[i];
            if let Some(scored_tags) = observation.get_value(morph) {
                if i == tokens.len() - 1 {
                    for st in scored_tags {
                        if st.tag_id == pos_id {
                            self.put(self.irr_idx, end_idx, morph, pos_id, st.score);
                            if pos_id == self.sejong_tags.ec_id {
                                self.put(self.irr_idx, end_idx, morph, self.sejong_tags.ef_id, st.score);
                            }
                        }
                    }
                    let mut irr_extend = LatticeNode::new(self.irr_idx, end_idx, morph.clone(), constant::IRREGULAR_ID, 0.0);
                    irr_extend.prev_node_idx = 0;
                    self.append_node(irr_extend);
                } else {
                    for st in scored_tags {
                        if st.tag_id == pos_id {
                            self.put(self.irr_idx, self.irr_idx - 1, morph, pos_id, st.score);
                        }
                    }
                }
            }
            self.irr_idx -= 1;
        }
    }

    /// 기분석 사전 처리
    pub fn put_fwd(&mut self, begin_idx: i32, end_idx: i32, fwd_list: &[(String, String)]) {
        if fwd_list.len() == 1 {
            let tag_id = self.pos_table.get_id(&fwd_list[0].1);
            self.put(begin_idx, end_idx, &fwd_list[0].0, tag_id, 0.0);
        } else {
            for (i, (morph, tag)) in fwd_list.iter().enumerate() {
                let tag_id = self.pos_table.get_id(tag);
                if i == 0 {
                    self.put(begin_idx, self.irr_idx - 1, morph, tag_id, 0.0);
                } else if i == fwd_list.len() - 1 {
                    self.put(self.irr_idx, end_idx, morph, tag_id, 0.0);
                } else {
                    self.put(self.irr_idx, self.irr_idx - 1, morph, tag_id, 0.0);
                }
                self.irr_idx -= 1;
            }
        }
    }

    pub fn append_node(&mut self, node: LatticeNode) -> usize {
        let end_idx = node.end_idx;
        if end_idx >= 0 {
            let idx = end_idx as usize;
            self.ensure_capacity(idx);
            if self.positive_nodes[idx].is_none() {
                self.positive_nodes[idx] = Some(Vec::new());
            }
            let list = self.positive_nodes[idx].as_mut().unwrap();
            list.push(node);
            list.len() - 1
        } else {
            let list = self.negative_nodes.entry(end_idx).or_insert_with(Vec::new);
            list.push(node);
            list.len() - 1
        }
    }

    pub fn append_end_node(&mut self) -> bool {
        self.put(self.last_idx, self.last_idx + 1, constant::EOE, self.sejong_tags.eoe_id, 0.0)
    }

    fn get_max_transition(
        &self, prev_nodes: &[LatticeNode], begin_idx: i32, end_idx: i32,
        morph: &str, tag_id: i32, score: f64,
    ) -> Option<LatticeNode> {
        let mut prev_max_score = f64::NEG_INFINITY;
        let mut prev_max_idx: i32 = -1;
        let col_scores = self.transition.get_column_scores(tag_id);

        for (i, prev_node) in prev_nodes.iter().enumerate() {
            let prev_tag_id = prev_node.tag_id;
            if prev_tag_id == -1 { continue; }

            let (actual_prev_id, prev_morph) = if prev_tag_id == self.sejong_tags.eoe_id {
                (self.sejong_tags.boe_id, constant::BOE)
            } else {
                (prev_tag_id, prev_node.morph.as_str())
            };

            if actual_prev_id < 0 || actual_prev_id as usize >= col_scores.len() { continue; }
            let trans_score = col_scores[actual_prev_id as usize];
            if trans_score == f64::NEG_INFINITY { continue; }

            if !self.rule_checker.is_valid_rule(prev_morph, actual_prev_id, morph, tag_id) {
                continue;
            }

            let total = trans_score + prev_node.score;
            if total > prev_max_score {
                prev_max_score = total;
                prev_max_idx = i as i32;
            }
        }

        if prev_max_idx != -1 {
            let mut node = LatticeNode::new(begin_idx, end_idx, morph.to_string(), tag_id, prev_max_score + score);
            node.prev_node_idx = prev_max_idx;
            Some(node)
        } else {
            None
        }
    }

    fn get_nbest_max_transition(
        &self, prev_nodes: &[LatticeNode], begin_idx: i32, end_idx: i32,
        morph: &str, tag_id: i32, score: f64,
    ) -> Option<Vec<LatticeNode>> {
        let mut nbest_list: Vec<LatticeNode> = Vec::new();
        let col_scores = self.transition.get_column_scores(tag_id);

        for (i, prev_node) in prev_nodes.iter().enumerate() {
            let prev_tag_id = prev_node.tag_id;
            if prev_tag_id == -1 { continue; }

            let (actual_prev_id, prev_morph) = if prev_tag_id == self.sejong_tags.eoe_id {
                (self.sejong_tags.boe_id, constant::BOE)
            } else {
                (prev_tag_id, prev_node.morph.as_str())
            };

            if actual_prev_id < 0 || actual_prev_id as usize >= col_scores.len() { continue; }
            let trans_score = col_scores[actual_prev_id as usize];
            if trans_score == f64::NEG_INFINITY { continue; }

            if !self.rule_checker.is_valid_rule(prev_morph, actual_prev_id, morph, tag_id) {
                continue;
            }

            let total = trans_score + prev_node.score + score;

            if nbest_list.len() < self.nbest {
                let mut node = LatticeNode::new(begin_idx, end_idx, morph.to_string(), tag_id, total);
                node.prev_node_idx = i as i32;
                nbest_list.push(node);
            } else {
                let mut min_idx = 0;
                let mut min_score = nbest_list[0].score;
                for j in 1..nbest_list.len() {
                    if nbest_list[j].score < min_score {
                        min_idx = j;
                        min_score = nbest_list[j].score;
                    }
                }
                if min_score < total {
                    let mut node = LatticeNode::new(begin_idx, end_idx, morph.to_string(), tag_id, total);
                    node.prev_node_idx = i as i32;
                    nbest_list[min_idx] = node;
                }
            }
        }

        if nbest_list.is_empty() { None } else { Some(nbest_list) }
    }

    pub fn find_nbest_path(&self) -> Option<Vec<Vec<LatticeNode>>> {
        let idx = self.last_idx + 1;
        let end_nodes = self.get_node_list(idx)?;

        let mut nbest_paths = Vec::new();

        for end_node in end_nodes {
            let mut path = Vec::new();
            let mut cur_begin_idx = end_node.begin_idx;
            let mut cur_prev_node_idx = end_node.prev_node_idx;
            let mut prev_end_idx = end_node.end_idx;

            loop {
                let prev_list = self.get_node_list(cur_begin_idx)?;
                let prev_node = &prev_list[cur_prev_node_idx as usize];

                let actual_end_idx = if prev_node.end_idx < 0 { prev_end_idx } else { prev_node.end_idx };

                path.push(LatticeNode {
                    begin_idx: prev_node.begin_idx,
                    end_idx: actual_end_idx,
                    morph: prev_node.morph.clone(),
                    tag_id: prev_node.tag_id,
                    score: prev_node.score,
                    prev_node_idx: prev_node.prev_node_idx,
                });

                prev_end_idx = actual_end_idx;
                if prev_node.begin_idx == 0 {
                    break;
                }
                cur_begin_idx = prev_node.begin_idx;
                cur_prev_node_idx = prev_node.prev_node_idx;
            }

            nbest_paths.push(path);
        }

        if nbest_paths.len() > 1 {
            nbest_paths.sort_by(|a, b| {
                let score_a = a.first().map(|n| n.score).unwrap_or(f64::NEG_INFINITY);
                let score_b = b.first().map(|n| n.score).unwrap_or(f64::NEG_INFINITY);
                score_b.partial_cmp(&score_a).unwrap_or(std::cmp::Ordering::Equal)
            });
        }

        Some(nbest_paths)
    }
}
