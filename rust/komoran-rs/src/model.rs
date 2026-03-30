/// 모델 로딩 모듈
/// Resources.java, Transition.java, PosTable.java 포팅

use std::collections::HashMap;
use std::path::Path;
use std::fs;

use crate::trie::{AhoCorasickTrie, ScoredTag, IrregularNode, load_observation_trie, load_irregular_trie};
use crate::constant::SejongTags;

/// 전이 확률 행렬
#[derive(Clone)]
pub struct Transition {
    pub score_matrix: Vec<Vec<f64>>,
    /// 전치 행렬: column_scores[cur_id][prev_id] = score_matrix[prev_id][cur_id]
    pub column_scores: Vec<Vec<f64>>,
    pub size: usize,
}

impl Transition {
    pub fn load(data: &[u8]) -> Self {
        let size = i32::from_be_bytes([data[0], data[1], data[2], data[3]]) as usize;
        let mut score_matrix = vec![vec![0.0f64; size]; size];
        let mut pos = 4;

        for i in 0..size {
            for j in 0..size {
                let mut bytes = [0u8; 8];
                bytes.copy_from_slice(&data[pos..pos + 8]);
                score_matrix[i][j] = f64::from_be_bytes(bytes);
                pos += 8;
            }
        }

        // 전치 행렬 빌드
        let mut column_scores = vec![vec![0.0f64; size]; size];
        for prev in 0..size {
            for cur in 0..size {
                column_scores[cur][prev] = score_matrix[prev][cur];
            }
        }

        Transition { score_matrix, column_scores, size }
    }

    #[inline]
    pub fn get_score(&self, prev_id: i32, cur_id: i32) -> f64 {
        if prev_id < 0 || cur_id < 0 { return f64::NEG_INFINITY; }
        self.score_matrix[prev_id as usize][cur_id as usize]
    }

    #[inline]
    pub fn get_column_scores(&self, cur_id: i32) -> &[f64] {
        &self.column_scores[cur_id as usize]
    }

    #[inline]
    pub fn has_transition(&self, prev_id: i32, cur_id: i32) -> bool {
        if prev_id < 0 || cur_id < 0 { return false; }
        self.score_matrix[prev_id as usize][cur_id as usize] != f64::NEG_INFINITY
    }
}

/// 품사 테이블
#[derive(Clone)]
pub struct PosTable {
    pub pos_to_id: HashMap<String, i32>,
    pub id_to_pos: HashMap<i32, String>,
}

impl PosTable {
    pub fn load(content: &str) -> Self {
        let mut pos_to_id = HashMap::new();
        let mut id_to_pos = HashMap::new();

        for line in content.lines() {
            let parts: Vec<&str> = line.split('\t').collect();
            if parts.len() == 2 {
                let pos = parts[0].to_string();
                let id: i32 = parts[1].parse().unwrap_or(-1);
                pos_to_id.insert(pos.clone(), id);
                id_to_pos.insert(id, pos);
            }
        }

        PosTable { pos_to_id, id_to_pos }
    }

    pub fn get_id(&self, pos: &str) -> i32 {
        *self.pos_to_id.get(pos).unwrap_or(&-1)
    }

    pub fn get_pos(&self, id: i32) -> Option<&str> {
        self.id_to_pos.get(&id).map(|s| s.as_str())
    }
}

/// TagUtil: 품사 분류 마스크
#[derive(Clone)]
pub struct TagUtil {
    noun_mask: Vec<bool>,
    eomi_mask: Vec<bool>,
    josa_mask: Vec<bool>,
}

impl TagUtil {
    pub fn new(pos_table: &PosTable) -> Self {
        let max_id = pos_table.id_to_pos.keys().copied().max().unwrap_or(0) as usize;
        let mut noun_mask = vec![false; max_id + 1];
        let mut eomi_mask = vec![false; max_id + 1];
        let mut josa_mask = vec![false; max_id + 1];

        for (&id, pos) in &pos_table.id_to_pos {
            if id < 0 { continue; }
            let idx = id as usize;
            let pos_str = pos.as_str();
            noun_mask[idx] = crate::constant::NOUN_TAGS.contains(&pos_str);
            eomi_mask[idx] = crate::constant::EOMI_TAGS.contains(&pos_str);
            josa_mask[idx] = crate::constant::JOSA_TAGS.contains(&pos_str);
        }

        TagUtil { noun_mask, eomi_mask, josa_mask }
    }

    #[inline]
    pub fn is_noun(&self, tag_id: i32) -> bool {
        if tag_id < 0 || tag_id as usize >= self.noun_mask.len() { return false; }
        self.noun_mask[tag_id as usize]
    }

    #[inline]
    pub fn is_eomi(&self, tag_id: i32) -> bool {
        if tag_id < 0 || tag_id as usize >= self.eomi_mask.len() { return false; }
        self.eomi_mask[tag_id as usize]
    }

    #[inline]
    pub fn is_josa(&self, tag_id: i32) -> bool {
        if tag_id < 0 || tag_id as usize >= self.josa_mask.len() { return false; }
        self.josa_mask[tag_id as usize]
    }
}

/// 전체 리소스
pub struct Resources {
    pub pos_table: PosTable,
    pub transition: Transition,
    pub observation: AhoCorasickTrie<Vec<ScoredTag>>,
    pub irregular: AhoCorasickTrie<Vec<IrregularNode>>,
    pub sejong_tags: SejongTags,
    pub tag_util: TagUtil,
}

impl Resources {
    /// 디렉토리에서 모델 로드
    pub fn load(model_path: &str) -> Self {
        let path = Path::new(model_path);

        // pos.table
        let pos_content = fs::read_to_string(path.join("pos.table"))
            .expect("Failed to read pos.table");
        let pos_table = PosTable::load(&pos_content);

        // SejongTags 초기화
        let mut sejong_tags = SejongTags::new();
        sejong_tags.build_from_pos_table(&pos_table.pos_to_id);

        // TagUtil
        let tag_util = TagUtil::new(&pos_table);

        // transition.dat
        let transition_data = fs::read(path.join("transition.dat"))
            .expect("Failed to read transition.dat");
        let transition = Transition::load(&transition_data);

        // observation.dat
        let observation_data = fs::read(path.join("observation.dat"))
            .expect("Failed to read observation.dat");
        let observation = load_observation_trie(&observation_data);

        // irregular.dat
        let irregular_data = fs::read(path.join("irregular.dat"))
            .expect("Failed to read irregular.dat");
        let irregular = load_irregular_trie(&irregular_data);

        Resources {
            pos_table,
            transition,
            observation,
            irregular,
            sejong_tags,
            tag_util,
        }
    }
}
