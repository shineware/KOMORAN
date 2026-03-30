/// 한글 자소 분해/합성 모듈
/// KoreanUnitParser.java 포팅 — O(1) 조회 테이블 최적화

const CHO_SUNG: [char; 19] = [
    '\u{3131}', '\u{3132}', '\u{3134}', '\u{3137}', '\u{3138}',
    '\u{3139}', '\u{3141}', '\u{3142}', '\u{3143}', '\u{3145}',
    '\u{3146}', '\u{3147}', '\u{3148}', '\u{3149}', '\u{314a}',
    '\u{314b}', '\u{314c}', '\u{314d}', '\u{314e}',
];

const JUNG_SUNG: [char; 21] = [
    '\u{314f}', '\u{3150}', '\u{3151}', '\u{3152}', '\u{3153}',
    '\u{3154}', '\u{3155}', '\u{3156}', '\u{3157}', '\u{3158}',
    '\u{3159}', '\u{315a}', '\u{315b}', '\u{315c}', '\u{315d}',
    '\u{315e}', '\u{315f}', '\u{3160}', '\u{3161}', '\u{3162}',
    '\u{3163}',
];

const JONG_SUNG: [char; 28] = [
    '\0',       '\u{3131}', '\u{3132}', '\u{3133}', '\u{3134}',
    '\u{3135}', '\u{3136}', '\u{3137}', '\u{3139}', '\u{313a}',
    '\u{313b}', '\u{313c}', '\u{313d}', '\u{313e}', '\u{313f}',
    '\u{3140}', '\u{3141}', '\u{3142}', '\u{3144}', '\u{3145}',
    '\u{3146}', '\u{3147}', '\u{3148}', '\u{314a}', '\u{314b}',
    '\u{314c}', '\u{314d}', '\u{314e}',
];

// ============ O(1) 역방향 조회 테이블 ============
// char → index 매핑 (0x3131-0x314E 범위, 30 entries)
// 0xFF = 해당 자모가 아닌 경우

/// 초성 조회: ㄱ→0, ㄲ→1, ㄴ→2, ... ㅎ→18
const CHO_INDEX_TABLE: [u8; 30] = [
    0,    // 0x3131 ㄱ
    1,    // 0x3132 ㄲ
    0xFF, // 0x3133 ㄳ
    2,    // 0x3134 ㄴ
    0xFF, // 0x3135 ㄵ
    0xFF, // 0x3136 ㄶ
    3,    // 0x3137 ㄷ
    4,    // 0x3138 ㄸ
    5,    // 0x3139 ㄹ
    0xFF, // 0x313A ㄺ
    0xFF, // 0x313B ㄻ
    0xFF, // 0x313C ㄼ
    0xFF, // 0x313D ㄽ
    0xFF, // 0x313E ㄾ
    0xFF, // 0x313F ㄿ
    0xFF, // 0x3140 ㅀ
    6,    // 0x3141 ㅁ
    7,    // 0x3142 ㅂ
    8,    // 0x3143 ㅃ
    0xFF, // 0x3144 ㅄ
    9,    // 0x3145 ㅅ
    10,   // 0x3146 ㅆ
    11,   // 0x3147 ㅇ
    12,   // 0x3148 ㅈ
    13,   // 0x3149 ㅉ
    14,   // 0x314A ㅊ
    15,   // 0x314B ㅋ
    16,   // 0x314C ㅌ
    17,   // 0x314D ㅍ
    18,   // 0x314E ㅎ
];

/// 종성 조회: ㄱ→1, ㄲ→2, ... ㅎ→27, 0xFF=종성불가
const JONG_INDEX_TABLE: [u8; 30] = [
    1,    // 0x3131 ㄱ
    2,    // 0x3132 ㄲ
    3,    // 0x3133 ㄳ
    4,    // 0x3134 ㄴ
    5,    // 0x3135 ㄵ
    6,    // 0x3136 ㄶ
    7,    // 0x3137 ㄷ
    0xFF, // 0x3138 ㄸ (종성 불가)
    8,    // 0x3139 ㄹ
    9,    // 0x313A ㄺ
    10,   // 0x313B ㄻ
    11,   // 0x313C ㄼ
    12,   // 0x313D ㄽ
    13,   // 0x313E ㄾ
    14,   // 0x313F ㄿ
    15,   // 0x3140 ㅀ
    16,   // 0x3141 ㅁ
    17,   // 0x3142 ㅂ
    0xFF, // 0x3143 ㅃ (종성 불가)
    18,   // 0x3144 ㅄ
    19,   // 0x3145 ㅅ
    20,   // 0x3146 ㅆ
    21,   // 0x3147 ㅇ
    22,   // 0x3148 ㅈ
    0xFF, // 0x3149 ㅉ (종성 불가)
    23,   // 0x314A ㅊ
    24,   // 0x314B ㅋ
    25,   // 0x314C ㅌ
    26,   // 0x314D ㅍ
    27,   // 0x314E ㅎ
];

/// 초성 인덱스 O(1) 조회
#[inline]
fn cho_index(ch: char) -> Option<usize> {
    let c = ch as u32;
    if c < 0x3131 || c > 0x314E { return None; }
    let idx = CHO_INDEX_TABLE[(c - 0x3131) as usize];
    if idx == 0xFF { None } else { Some(idx as usize) }
}

/// 중성 인덱스 O(1) 조회 (0x314F-0x3163 연속)
#[inline]
fn jung_index(ch: char) -> Option<usize> {
    let c = ch as u32;
    if c < 0x314F || c > 0x3163 { return None; }
    Some((c - 0x314F) as usize)
}

/// 종성 인덱스 O(1) 조회
#[inline]
fn jong_index(ch: char) -> Option<usize> {
    let c = ch as u32;
    if c < 0x3131 || c > 0x314E { return None; }
    let idx = JONG_INDEX_TABLE[(c - 0x3131) as usize];
    if idx == 0xFF { None } else { Some(idx as usize) }
}

/// 한글 음절인지 확인 (0xAC00 ~ 0xD7A3)
#[inline]
fn is_hangul_syllable(ch: char) -> bool {
    let c = ch as u32;
    (0xAC00..=0xD7A3).contains(&c)
}

/// 한글 문자열을 자소 단위로 분해
pub fn parse(s: &str) -> String {
    let mut result = String::with_capacity(s.len() * 3);
    for ch in s.chars() {
        if is_hangul_syllable(ch) {
            let tmp = ch as u32 - 0xAC00;
            let cho = (tmp / (21 * 28)) as usize;
            let remainder = tmp % (21 * 28);
            let jung = (remainder / 28) as usize;
            let jong = (remainder % 28) as usize;
            result.push(CHO_SUNG[cho]);
            result.push(JUNG_SUNG[jung]);
            if jong != 0 {
                result.push(JONG_SUNG[jong]);
            }
        } else {
            result.push(ch);
        }
    }
    result
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum UnitType {
    ChoSung,
    JungSung,
    JongSung,
    Other,
}

/// 자소 분해 (타입 정보 포함)
pub fn parse_with_type(s: &str) -> Vec<(char, UnitType)> {
    let mut result = Vec::with_capacity(s.len() * 3);
    for ch in s.chars() {
        if is_hangul_syllable(ch) {
            let tmp = ch as u32 - 0xAC00;
            let cho = (tmp / (21 * 28)) as usize;
            let remainder = tmp % (21 * 28);
            let jung = (remainder / 28) as usize;
            let jong = (remainder % 28) as usize;
            result.push((CHO_SUNG[cho], UnitType::ChoSung));
            result.push((JUNG_SUNG[jung], UnitType::JungSung));
            if jong != 0 {
                result.push((JONG_SUNG[jong], UnitType::JongSung));
            }
        } else {
            result.push((ch, UnitType::Other));
        }
    }
    result
}

/// 자소를 음절로 합성 (타입 정보 사용)
pub fn combine_with_type(units: &[(char, UnitType)]) -> String {
    let mut result = String::with_capacity(units.len());
    let mut chosung: usize = 0;
    let mut jungsung: usize = 0;
    let mut jongsung: usize = 0;
    let mut has_buffer = false;

    for &(ch, unit_type) in units {
        match unit_type {
            UnitType::ChoSung => {
                if has_buffer {
                    let syllable = char::from_u32(0xAC00 + (chosung as u32) * 588 + (jungsung as u32) * 28 + (jongsung as u32)).unwrap_or('?');
                    result.push(syllable);
                    jungsung = 0;
                    jongsung = 0;
                }
                chosung = cho_index(ch).unwrap_or(0);
                has_buffer = true;
            }
            UnitType::JungSung => {
                jungsung = jung_index(ch).unwrap_or(0);
                has_buffer = true;
            }
            UnitType::JongSung => {
                jongsung = jong_index(ch).unwrap_or(0);
                has_buffer = true;
            }
            UnitType::Other => {
                if has_buffer {
                    let syllable = char::from_u32(0xAC00 + (chosung as u32) * 588 + (jungsung as u32) * 28 + (jongsung as u32)).unwrap_or('?');
                    result.push(syllable);
                    chosung = 0;
                    jungsung = 0;
                    jongsung = 0;
                }
                result.push(ch);
                has_buffer = false;
            }
        }
    }
    if has_buffer {
        let syllable = char::from_u32(0xAC00 + (chosung as u32) * 588 + (jungsung as u32) * 28 + (jongsung as u32)).unwrap_or('?');
        result.push(syllable);
    }
    result
}

/// 자소를 음절로 합성 (문자열 기반)
pub fn combine(s: &str) -> String {
    let chars: Vec<char> = s.chars().collect();
    let len = chars.len();
    let mut result = String::with_capacity(s.len());
    let mut prev_idx = 0;

    let mut i = 1;
    while i < len {
        let ch = chars[i];
        if let Some(jungsung) = jung_index(ch) {
            if let Some(chosung) = cho_index(chars[i - 1]) {
                // 이전 문자들을 그대로 추가
                for j in prev_idx..i - 1 {
                    result.push(chars[j]);
                }

                let mut jongsung: usize = 0;
                if i + 1 < len {
                    jongsung = jong_index(chars[i + 1]).unwrap_or(0);
                }
                if i + 2 < len && jung_index(chars[i + 2]).is_some() {
                    jongsung = 0;
                }

                let syllable = char::from_u32(
                    0xAC00 + (chosung as u32) * 588 + (jungsung as u32) * 28 + (jongsung as u32)
                ).unwrap_or('?');
                result.push(syllable);

                if jongsung > 0 {
                    i += 1;
                }
                prev_idx = i + 1;
            }
        }
        i += 1;
    }
    if prev_idx < len {
        for j in prev_idx..len {
            result.push(chars[j]);
        }
    }
    result
}

/// 음절 영역 리스트 생성 (자소 인덱스 -> 음절 인덱스 매핑)
pub fn get_syllable_area_list(s: &str) -> Vec<(usize, usize)> {
    let chars: Vec<char> = s.chars().collect();
    let len = chars.len();
    let mut syllable_area_list: Vec<(usize, usize)> = Vec::with_capacity(len);
    let mut prev_idx = 0;

    let mut i = 1;
    while i < len {
        let ch = chars[i];
        if let Some(_jungsung) = jung_index(ch) {
            if let Some(_chosung) = cho_index(chars[i - 1]) {
                // 이전 영역의 분할된 음절 추가
                if i - 1 > prev_idx {
                    for j in prev_idx..i - 1 {
                        syllable_area_list.push((j, j + 1));
                    }
                }

                let mut jongsung: usize = 0;
                if i + 1 < len {
                    jongsung = jong_index(chars[i + 1]).unwrap_or(0);
                }
                if i + 2 < len && jung_index(chars[i + 2]).is_some() {
                    jongsung = 0;
                }

                let begin_index = i - 1;
                if jongsung > 0 {
                    i += 1;
                }
                let end_index = i;
                syllable_area_list.push((begin_index, end_index + 1));
                prev_idx = i + 1;
            }
        }
        i += 1;
    }
    if prev_idx < len {
        for j in prev_idx..len {
            syllable_area_list.push((j, j + 1));
        }
    }
    syllable_area_list
}

/// 종성이 있는지 확인 — O(1) 바이트 레벨 검사
/// 한글 자모 U+3131-U+314E는 UTF-8에서 3바이트 (E3 84/85 XX)
#[inline]
pub fn has_jongsung(morph: &str) -> bool {
    let bytes = morph.as_bytes();
    let len = bytes.len();
    if len < 3 { return false; }

    // 마지막 3바이트가 3-byte UTF-8 시퀀스인지 확인
    let b0 = bytes[len - 3];
    let b1 = bytes[len - 2];
    let b2 = bytes[len - 1];

    // 한글 호환 자모 영역은 E3으로 시작
    if b0 != 0xE3 { return false; }
    if b1 & 0xC0 != 0x80 || b2 & 0xC0 != 0x80 { return false; }

    let c = ((b0 as u32 & 0x0F) << 12) | ((b1 as u32 & 0x3F) << 6) | (b2 as u32 & 0x3F);
    if !(0x3131..=0x314E).contains(&c) { return false; }

    // ㄸ(0x3138), ㅃ(0x3143), ㅉ(0x3149)은 종성 불가
    c != 0x3138 && c != 0x3143 && c != 0x3149
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_parse() {
        let result = parse("감기");
        assert_eq!(result, "ㄱㅏㅁㄱㅣ");
    }

    #[test]
    fn test_combine() {
        let result = combine("ㄱㅏㅁㄱㅣ");
        assert_eq!(result, "감기");
    }

    #[test]
    fn test_parse_with_english() {
        let result = parse("Hello감기");
        assert_eq!(result, "Helloㄱㅏㅁㄱㅣ");
    }

    #[test]
    fn test_has_jongsung() {
        assert!(has_jongsung("ㄱㅏㅁ")); // ㅁ = 종성 가능
        assert!(!has_jongsung("ㄱㅏ"));  // ㅏ = 중성, 종성 아님
        assert!(!has_jongsung("ㄱㅏㅃ")); // ㅃ = 종성 불가
        assert!(has_jongsung("ㄱ"));      // ㄱ = 종성 가능
    }

    #[test]
    fn test_cho_index_o1() {
        assert_eq!(cho_index('\u{3131}'), Some(0)); // ㄱ
        assert_eq!(cho_index('\u{314E}'), Some(18)); // ㅎ
        assert_eq!(cho_index('\u{3133}'), None); // ㄳ = 초성 아님
    }

    #[test]
    fn test_jung_index_o1() {
        assert_eq!(jung_index('\u{314F}'), Some(0)); // ㅏ
        assert_eq!(jung_index('\u{3163}'), Some(20)); // ㅣ
        assert_eq!(jung_index('\u{3131}'), None); // ㄱ = 중성 아님
    }

    #[test]
    fn test_jong_index_o1() {
        assert_eq!(jong_index('\u{3131}'), Some(1)); // ㄱ
        assert_eq!(jong_index('\u{314E}'), Some(27)); // ㅎ
        assert_eq!(jong_index('\u{3138}'), None); // ㄸ = 종성 불가
    }
}
