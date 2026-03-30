/// 배열 기반 Aho-Corasick 오토마톤 (Rust 포팅)
/// DoubleArrayAhoCorasick.java → trie.rs

/// Aho-Corasick Trie에 저장되는 출력 값
#[derive(Clone, Debug)]
pub struct ScoredTag {
    pub tag: String,
    pub tag_id: i32,
    pub score: f64,
}

/// 불규칙 노드
#[derive(Clone, Debug)]
pub struct IrregularNode {
    pub last_morph: String,
    pub first_pos_id: i32,
    pub last_pos_id: i32,
    pub inner_score: f64,
    pub morph_format: String,
    pub tokens: Vec<(String, i32)>, // (morph, posId)
}

/// 배열 기반 Aho-Corasick 오토마톤
#[derive(Clone)]
pub struct AhoCorasickTrie<V: Clone> {
    fail: Vec<i32>,
    output: Vec<Option<V>>,
    child_keys: Vec<Vec<u32>>,   // 정렬된 자식 키
    child_values: Vec<Vec<i32>>, // 대응 자식 노드 ID
    key_cache: Vec<String>,
    key_char_counts: Vec<usize>, // 사전 계산된 char 개수 (O(1) begin_idx 계산용)
    node_count: usize,
}

impl<V: Clone> AhoCorasickTrie<V> {
    pub fn new() -> Self {
        AhoCorasickTrie {
            fail: vec![0],
            output: vec![None],
            child_keys: vec![vec![]],
            child_values: vec![vec![]],
            key_cache: vec![String::new()],
            key_char_counts: vec![0],
            node_count: 1,
        }
    }

    /// 자식 노드 탐색 (이진 탐색)
    #[inline]
    fn get_child(&self, node_id: usize, encoded_char: u32) -> Option<usize> {
        let keys = &self.child_keys[node_id];
        if keys.is_empty() { return None; }
        match keys.binary_search(&encoded_char) {
            Ok(idx) => Some(self.child_values[node_id][idx] as usize),
            Err(_) => None,
        }
    }

    /// 상태 전이 (fail link를 따라감)
    #[inline]
    pub fn advance(&self, mut state: usize, ch: char) -> usize {
        let c = encode_char(ch);
        while state != 0 {
            if let Some(child) = self.get_child(state, c) {
                return child;
            }
            state = self.fail[state] as usize;
        }
        self.get_child(0, c).unwrap_or(0)
    }

    /// 스트리밍 검색: 현재 상태에서 매칭된 모든 출력을 반환
    pub fn get(&self, context: &mut FindContext) -> Vec<(&str, &V)> {
        let mut results = Vec::new();
        let mut s = context.state;
        while s != 0 {
            if let Some(ref val) = self.output[s] {
                results.push((self.key_cache[s].as_str(), val));
            }
            s = self.fail[s] as usize;
        }
        results
    }

    /// 스트리밍 검색: advance + get
    pub fn search(&self, context: &mut FindContext, ch: char) -> Vec<(&str, &V)> {
        context.state = self.advance(context.state, ch);
        self.get(context)
    }

    /// 특정 키의 값 조회
    pub fn get_value(&self, key: &str) -> Option<&V> {
        let mut state: usize = 0;
        for ch in key.chars() {
            let c = encode_char(ch);
            match self.get_child(state, c) {
                Some(child) => state = child,
                None => return None,
            }
        }
        self.output[state].as_ref()
    }

    /// char 배열로 자식 존재 여부 확인
    pub fn has_child(&self, chars: &[char]) -> bool {
        let mut state: usize = 0;
        for &ch in chars {
            let c = encode_char(ch);
            match self.get_child(state, c) {
                Some(child) => state = child,
                None => return false,
            }
        }
        true
    }

    /// 재사용 버퍼 검색: Vec 할당 제거 + char count 포함
    /// 반환: (key, value, char_count)
    pub fn search_into<'t>(&'t self, context: &mut FindContext, ch: char, results: &mut Vec<(&'t str, &'t V, usize)>) {
        context.state = self.advance(context.state, ch);
        results.clear();
        let mut s = context.state;
        while s != 0 {
            if let Some(ref val) = self.output[s] {
                results.push((self.key_cache[s].as_str(), val, self.key_char_counts[s]));
            }
            s = self.fail[s] as usize;
        }
    }

    pub fn new_find_context(&self) -> FindContext {
        FindContext { state: 0 }
    }
}

/// 스트리밍 검색용 컨텍스트
pub struct FindContext {
    pub state: usize,
}

/// 자소 문자를 compact 정수로 인코딩
/// JasoEncoder.java와 동일한 매핑
fn encode_char(ch: char) -> u32 {
    let c = ch as u32;
    // 호환 자모 영역 (0x3131 ~ 0x3163)
    if (0x3131..=0x3163).contains(&c) {
        return c - 0x3131 + 1;
    }
    // ASCII printable (0x20 ~ 0x7E)
    if (0x20..=0x7E).contains(&c) {
        return c - 0x20 + 52; // 51자모 + 1(reserved)
    }
    // 한글 자모 확장 (0x1100 ~ 0x11FF)
    if (0x1100..=0x11FF).contains(&c) {
        return c - 0x1100 + 147; // 51 + 95 + 1
    }
    // 기타: 해시 기반 동적 할당 대신 고정 오프셋
    c % 512 + 403
}

/// Java DataOutputStream 형식에서 Trie 로드
pub fn load_observation_trie(data: &[u8]) -> AhoCorasickTrie<Vec<ScoredTag>> {
    let mut cursor = DataCursor::new(data);
    let node_count = cursor.read_i32() as usize;

    // fail array
    let mut fail = Vec::with_capacity(node_count);
    for _ in 0..node_count {
        fail.push(cursor.read_i32());
    }

    // child keys & values
    let mut child_keys = Vec::with_capacity(node_count);
    let mut child_values = Vec::with_capacity(node_count);
    for _ in 0..node_count {
        let len = cursor.read_i32() as usize;
        if len == 0 {
            child_keys.push(vec![]);
            child_values.push(vec![]);
        } else {
            let mut keys = Vec::with_capacity(len);
            let mut vals = Vec::with_capacity(len);
            for _ in 0..len {
                keys.push(cursor.read_i32() as u32);
                vals.push(cursor.read_i32());
            }
            child_keys.push(keys);
            child_values.push(vals);
        }
    }

    // outputs
    let mut output = Vec::with_capacity(node_count);
    for _ in 0..node_count {
        let len = cursor.read_i32() as usize;
        if len == 0 {
            output.push(None);
        } else {
            let mut tags = Vec::with_capacity(len);
            for _ in 0..len {
                let tag_id = cursor.read_i32();
                let score = cursor.read_f64();
                let tag = cursor.read_utf();
                tags.push(ScoredTag { tag, tag_id, score });
            }
            output.push(Some(tags));
        }
    }

    // key cache + char count 사전 계산
    let mut key_cache = Vec::with_capacity(node_count);
    let mut key_char_counts = Vec::with_capacity(node_count);
    for _ in 0..node_count {
        let key = cursor.read_utf();
        key_char_counts.push(key.chars().count());
        key_cache.push(key);
    }

    AhoCorasickTrie {
        fail,
        output,
        child_keys,
        child_values,
        key_cache,
        key_char_counts,
        node_count,
    }
}

/// Java DataOutputStream 형식에서 Irregular Trie 로드
pub fn load_irregular_trie(data: &[u8]) -> AhoCorasickTrie<Vec<IrregularNode>> {
    let mut cursor = DataCursor::new(data);
    let node_count = cursor.read_i32() as usize;

    // fail array
    let mut fail = Vec::with_capacity(node_count);
    for _ in 0..node_count {
        fail.push(cursor.read_i32());
    }

    // child keys & values
    let mut child_keys = Vec::with_capacity(node_count);
    let mut child_values = Vec::with_capacity(node_count);
    for _ in 0..node_count {
        let len = cursor.read_i32() as usize;
        if len == 0 {
            child_keys.push(vec![]);
            child_values.push(vec![]);
        } else {
            let mut keys = Vec::with_capacity(len);
            let mut vals = Vec::with_capacity(len);
            for _ in 0..len {
                keys.push(cursor.read_i32() as u32);
                vals.push(cursor.read_i32());
            }
            child_keys.push(keys);
            child_values.push(vals);
        }
    }

    // outputs
    let mut output = Vec::with_capacity(node_count);
    for _ in 0..node_count {
        let len = cursor.read_i32() as usize;
        if len == 0 {
            output.push(None);
        } else {
            let mut nodes = Vec::with_capacity(len);
            for _ in 0..len {
                let last_morph = cursor.read_utf();
                let first_pos_id = cursor.read_i32();
                let last_pos_id = cursor.read_i32();
                let inner_score = cursor.read_f64();
                let morph_format = cursor.read_utf();
                let token_count = cursor.read_i32() as usize;
                let mut tokens = Vec::with_capacity(token_count);
                for _ in 0..token_count {
                    let morph = cursor.read_utf();
                    let pos_id = cursor.read_i32();
                    tokens.push((morph, pos_id));
                }
                nodes.push(IrregularNode {
                    last_morph,
                    first_pos_id,
                    last_pos_id,
                    inner_score,
                    morph_format,
                    tokens,
                });
            }
            output.push(Some(nodes));
        }
    }

    // key cache + char count 사전 계산
    let mut key_cache = Vec::with_capacity(node_count);
    let mut key_char_counts = Vec::with_capacity(node_count);
    for _ in 0..node_count {
        let key = cursor.read_utf();
        key_char_counts.push(key.chars().count());
        key_cache.push(key);
    }

    AhoCorasickTrie {
        fail,
        output,
        child_keys,
        child_values,
        key_cache,
        key_char_counts,
        node_count,
    }
}

/// Java DataOutputStream 바이너리 형식 파서
struct DataCursor<'a> {
    data: &'a [u8],
    pos: usize,
}

impl<'a> DataCursor<'a> {
    fn new(data: &'a [u8]) -> Self {
        DataCursor { data, pos: 0 }
    }

    fn read_i32(&mut self) -> i32 {
        let bytes = [self.data[self.pos], self.data[self.pos + 1],
                     self.data[self.pos + 2], self.data[self.pos + 3]];
        self.pos += 4;
        i32::from_be_bytes(bytes)
    }

    fn read_f64(&mut self) -> f64 {
        let mut bytes = [0u8; 8];
        bytes.copy_from_slice(&self.data[self.pos..self.pos + 8]);
        self.pos += 8;
        f64::from_be_bytes(bytes)
    }

    /// Java DataOutputStream.writeUTF 형식 읽기
    fn read_utf(&mut self) -> String {
        let len = u16::from_be_bytes([self.data[self.pos], self.data[self.pos + 1]]) as usize;
        self.pos += 2;
        if len == 0 {
            return String::new();
        }
        // Java Modified UTF-8 decoding
        let mut chars = Vec::new();
        let end = self.pos + len;
        while self.pos < end {
            let b1 = self.data[self.pos] as u32;
            if b1 < 0x80 {
                chars.push(char::from_u32(b1).unwrap_or('?'));
                self.pos += 1;
            } else if b1 & 0xE0 == 0xC0 {
                let b2 = self.data[self.pos + 1] as u32;
                let cp = ((b1 & 0x1F) << 6) | (b2 & 0x3F);
                chars.push(char::from_u32(cp).unwrap_or('?'));
                self.pos += 2;
            } else if b1 & 0xF0 == 0xE0 {
                let b2 = self.data[self.pos + 1] as u32;
                let b3 = self.data[self.pos + 2] as u32;
                let cp = ((b1 & 0x0F) << 12) | ((b2 & 0x3F) << 6) | (b3 & 0x3F);
                chars.push(char::from_u32(cp).unwrap_or('?'));
                self.pos += 3;
            } else {
                self.pos += 1;
            }
        }
        chars.into_iter().collect()
    }
}
