# KOMORAN 사용 가이드

KOMORAN 한국어 형태소 분석기의 Rust 구현체입니다. Python 바인딩(PyO3)을 통해 Python에서 직접 사용할 수 있으며, Rust 라이브러리로도 사용 가능합니다.

## 목차

- [설치](#설치)
- [Python 사용법](#python-사용법)
- [Rust 사용법](#rust-사용법)
- [API 레퍼런스](#api-레퍼런스)
- [품사 태그표](#품사-태그표)
- [모델](#모델)
- [성능](#성능)

---

## 설치

### Python

```bash
cd rust/komoran-rs
pip install .
```

모델 파일이 패키지에 내장되어 있어 별도 다운로드가 필요 없습니다.

개발 모드로 설치하려면:

```bash
pip install maturin
maturin develop --release
```

### Rust

`Cargo.toml`에 로컬 경로로 추가:

```toml
[dependencies]
komoran-rs = { path = "../path/to/komoran-rs" }
```

---

## Python 사용법

### 기본 사용

```python
from komoran import Komoran

# 내장 모델 자동 사용 (인자 없이 생성)
komoran = Komoran()

# 또는 커스텀 모델 경로 지정
# komoran = Komoran("/path/to/custom/model")

komoran.analyze("아버지가방에들어가신다")
# '아버지/NNG 가방/NNG 에/JKB 들어가/VV 시/EP ㄴ다/EC'
```

### 형태소 분석 — `analyze()`

문장을 분석하여 `형태소/품사` 형식의 문자열을 반환합니다.

```python
komoran.analyze("대한민국은 민주공화국이다")
# '대한민국/NNP 은/JX 민주/NNG 공화국/NNG 이/VCP 다/EC'

komoran.analyze("오늘 날씨가 정말 좋다")
# '오늘/MAG 날씨/NNG 가/JKS 정말/MAG 좋/VA 다/EC'
```

### 명사 추출 — `nouns()`

NNG(일반명사)와 NNP(고유명사)를 추출합니다.

```python
komoran.nouns("서울특별시는 대한민국의 수도이다")
# ['서울특별시', '대한민국', '수도']
```

### 품사 태깅 — `pos()`

(형태소, 품사) 튜플의 리스트를 반환합니다.

```python
komoran.pos("나는 밥을 먹었다")
# [('나', 'NP'), ('는', 'JX'), ('밥', 'NNG'), ('을', 'JKO'),
#  ('먹', 'VV'), ('었', 'EP'), ('다', 'EC')]
```

### 토큰 리스트 — `tokens()`

형태소, 품사, 원문 위치 정보를 포함한 토큰 객체 리스트를 반환합니다.

```python
for token in komoran.tokens("대한민국은 민주공화국이다"):
    print(f"{token.morph}/{token.pos} [{token.begin_index}:{token.end_index}]")

# 대한민국/NNP [0:4]
# 은/JX [4:5]
# 민주/NNG [6:8]
# 공화국/NNG [8:11]
# 이/VCP [11:12]
# 다/EC [12:13]
```

`Token` 객체의 속성:

| 속성 | 타입 | 설명 |
|------|------|------|
| `morph` | `str` | 형태소 |
| `pos` | `str` | 품사 태그 |
| `begin_index` | `int` | 원문에서 시작 위치 (글자 단위, inclusive) |
| `end_index` | `int` | 원문에서 끝 위치 (글자 단위, exclusive) |

### 배치 분석 — `analyze_batch()`, `tokens_batch()`

여러 문장을 한 번에 분석합니다. 내부적으로 Rust rayon 멀티스레드로 병렬 처리되어 개별 호출보다 훨씬 빠릅니다.

```python
sentences = [
    "감기는 자주 걸리는 병이다",
    "대한민국은 민주공화국이다",
    "아버지가방에들어가신다",
]

# 배치 형태소 분석
results = komoran.analyze_batch(sentences)
for s, r in zip(sentences, results):
    print(f"{s} → {r}")

# 배치 토큰 분석
token_results = komoran.tokens_batch(sentences)
for s, tokens in zip(sentences, token_results):
    token_str = " ".join(f"{t.morph}/{t.pos}" for t in tokens)
    print(f"{s} → {token_str}")
```

### 실전 예제: 문서에서 명사 빈도 추출

```python
from collections import Counter
from komoran import Komoran

komoran = Komoran()

document = [
    "인공지능 기술이 빠르게 발전하고 있다",
    "인공지능은 다양한 분야에서 활용된다",
    "기술 발전이 사회에 미치는 영향이 크다",
]

# 배치 분석으로 전체 문서의 토큰 추출
all_tokens = komoran.tokens_batch(document)

# 명사만 추출하여 빈도 계산
nouns = []
for tokens in all_tokens:
    for t in tokens:
        if t.pos in ("NNG", "NNP"):
            nouns.append(t.morph)

counter = Counter(nouns)
for word, count in counter.most_common(10):
    print(f"{word}: {count}")
```

---

## Rust 사용법

### 기본 사용

```rust
use komoran_rs::komoran::KomoranEngine;

fn main() {
    // 모델 로드
    let engine = KomoranEngine::new("model");

    // 형태소 분석 (nbest=1)
    let results = engine.analyze("아버지가방에들어가신다", 1);
    let result = &results[0];

    // 평문 텍스트 출력
    println!("{}", result.get_plain_text());
    // 아버지/NNG 가방/NNG 에/JKB 들어가/VV 시/EP ㄴ다/EC
}
```

### 다양한 출력 형식

```rust
use komoran_rs::komoran::KomoranEngine;

fn main() {
    let engine = KomoranEngine::new("model");
    let results = engine.analyze("나는 밥을 먹었다", 1);
    let result = &results[0];

    // 1. 평문 텍스트
    println!("{}", result.get_plain_text());
    // 나/NP 는/JX 밥/NNG 을/JKO 먹/VV 었/EP 다/EC

    // 2. 명사 추출
    let nouns = result.get_nouns();
    println!("{:?}", nouns);
    // ["밥"]

    // 3. (형태소, 품사) 리스트
    let pos_list = result.get_list();
    for (morph, pos) in &pos_list {
        println!("{}/{}", morph, pos);
    }

    // 4. 토큰 (위치 정보 포함)
    let tokens = result.get_token_list();
    for token in &tokens {
        println!("{}/{} [{}:{}]", token.morph, token.pos, token.begin_index, token.end_index);
    }
}
```

### 배치 처리 (rayon 멀티스레드)

```rust
use komoran_rs::komoran::KomoranEngine;
use rayon::prelude::*;

fn main() {
    let engine = KomoranEngine::new("model");

    let sentences = vec![
        "감기는 자주 걸리는 병이다",
        "대한민국은 민주공화국이다",
        "아버지가방에들어가신다",
    ];

    // 멀티스레드 병렬 분석
    let results: Vec<String> = sentences.par_iter()
        .map(|s| {
            let result = engine.analyze(s, 1);
            result[0].get_plain_text()
        })
        .collect();

    for (s, r) in sentences.iter().zip(results.iter()) {
        println!("{} → {}", s, r);
    }
}
```

### 특정 품사 추출

```rust
use komoran_rs::komoran::KomoranEngine;
use komoran_rs::constant;

fn main() {
    let engine = KomoranEngine::new("model");
    let results = engine.analyze("빠른 갈색 여우가 게으른 개를 뛰어넘었다", 1);
    let result = &results[0];

    // 동사(VV)만 추출
    let verbs = result.get_morphs_by_tags(&[constant::VV]);
    println!("동사: {:?}", verbs);

    // 형용사(VA)만 추출
    let adjectives = result.get_morphs_by_tags(&[constant::VA]);
    println!("형용사: {:?}", adjectives);
}
```

---

## API 레퍼런스

### Python API

#### `Komoran(model_path=None)`

KOMORAN 분석기를 초기화합니다.

- `model_path`: 모델 파일이 있는 디렉토리 경로. 생략하면 내장 모델을 사용합니다.

#### `analyze(sentence: str) -> str`

형태소 분석 결과를 `형태소/품사` 형식의 문자열로 반환합니다.

#### `nouns(sentence: str) -> list[str]`

문장에서 명사(NNG, NNP)를 추출하여 리스트로 반환합니다.

#### `pos(sentence: str) -> list[tuple[str, str]]`

`(형태소, 품사)` 튜플의 리스트를 반환합니다.

#### `tokens(sentence: str) -> list[Token]`

위치 정보를 포함한 `Token` 객체의 리스트를 반환합니다.

#### `analyze_batch(sentences: list[str]) -> list[str]`

여러 문장을 멀티스레드로 병렬 분석합니다. 대량 처리 시 개별 호출보다 약 6~7배 빠릅니다.

#### `tokens_batch(sentences: list[str]) -> list[list[Token]]`

여러 문장의 토큰을 멀티스레드로 병렬 추출합니다.

### Rust API

#### `KomoranEngine::new(model_path: &str) -> Self`

모델 디렉토리 경로로 분석 엔진을 초기화합니다. `KomoranEngine`은 `Send + Sync`로 스레드 안전합니다.

#### `KomoranEngine::analyze(sentence: &str, nbest: usize) -> Vec<KomoranResult>`

문장을 분석하여 N-best 결과를 반환합니다. 일반적으로 `nbest=1`을 사용합니다.

#### `KomoranResult` 메서드

| 메서드 | 반환 타입 | 설명 |
|--------|----------|------|
| `get_plain_text()` | `String` | `형태소/품사` 형식 문자열 |
| `get_nouns()` | `Vec<String>` | NNG, NNP 명사 추출 |
| `get_list()` | `Vec<(String, String)>` | (형태소, 품사) 쌍 |
| `get_token_list()` | `Vec<PyToken>` | 위치 정보 포함 토큰 |
| `get_morphs_by_tags(&[&str])` | `Vec<String>` | 특정 품사의 형태소 추출 |

---

## 품사 태그표

| 태그 | 설명 | 예시 |
|------|------|------|
| NNG | 일반명사 | 사람, 학교, 나라 |
| NNP | 고유명사 | 서울, 대한민국 |
| NNB | 의존명사 | 것, 수, 줄 |
| NP | 대명사 | 나, 너, 그 |
| NR | 수사 | 하나, 둘, 셋 |
| VV | 동사 | 먹다, 가다, 하다 |
| VA | 형용사 | 좋다, 크다, 아름답다 |
| VX | 보조용언 | 있다 (보조), 주다 (보조) |
| VCP | 긍정지정사 | 이다 |
| VCN | 부정지정사 | 아니다 |
| MM | 관형사 | 새, 헌, 모든 |
| MAG | 일반부사 | 매우, 아주, 정말 |
| MAJ | 접속부사 | 그리고, 그러나 |
| IC | 감탄사 | 와, 아 |
| JKS | 주격조사 | 이/가 |
| JKC | 보격조사 | 이/가 |
| JKG | 관형격조사 | 의 |
| JKO | 목적격조사 | 을/를 |
| JKB | 부사격조사 | 에, 에서, 로 |
| JKV | 호격조사 | 아, 야 |
| JKQ | 인용격조사 | 라고, 고 |
| JC | 접속조사 | 와, 과 |
| JX | 보조사 | 은/는, 도, 만 |
| EP | 선어말어미 | 었, 시, 겠 |
| EF | 종결어미 | 다, 요, 습니다 |
| EC | 연결어미 | 고, 서, 며 |
| ETN | 명사형전성어미 | 기, 음 |
| ETM | 관형형전성어미 | 는, ㄴ, ㄹ |
| XPN | 체언접두사 | 풋, 늦 |
| XSN | 명사파생접미사 | 님, 적 |
| XSV | 동사파생접미사 | 하, 되 |
| XSA | 형용사파생접미사 | 답, 롭 |
| XR | 어근 | |
| SF | 마침표/물음표/느낌표 | . ? ! |
| SP | 쉼표/가운뎃점/콜론/빗금 | , · : / |
| SS | 따옴표/괄호 | " ' ( ) |
| SE | 줄임표 | ... |
| SO | 붙임표 | - ~ |
| SW | 기타기호 | → % @ |
| SL | 외국어 | Hello, World |
| SH | 한자 | 韓國 |
| SN | 숫자 | 123, 2024 |
| NA | 분석불능 | |

---

## 모델

### 모델 파일 구조

```
model/
├── pos.table          # 품사-ID 매핑 (텍스트)
├── observation.dat    # 관측 확률 (Aho-Corasick Trie, 바이너리)
├── transition.dat     # 전이 확률 (double[][] 행렬, 바이너리)
└── irregular.dat      # 불규칙 활용 패턴 (Aho-Corasick Trie, 바이너리)
```

### 내장 모델

`pip install .`로 설치하면 경량 모델(models_light)이 패키지에 내장됩니다. `Komoran()` 인자 없이 바로 사용 가능합니다.

### 커스텀 모델 사용

Java에서 학습한 모델을 사용하려면 경로를 직접 지정합니다:

```python
komoran = Komoran("/path/to/custom/model")
```

### 모델 호환성

- 모델 학습은 **Java에서만** 가능합니다 (CorpusBuilder → ModelBuilder)
- 학습된 모델은 **Java, Rust, Python 모두에서** 동일하게 사용 가능합니다
- Java에서 새 코퍼스로 재학습 후 export하면 Rust/Python에서 즉시 사용할 수 있습니다

### 모델 종류

| 모델 | 설명 | 사전 크기 |
|------|------|-----------|
| `models_light` | 경량 모델 (패키지에 내장) | ~17MB |
| `models_full` | 전체 모델 (Java에서 export 필요) | ~21MB |

---

## 성능

100개 다양한 한국어 문장 × 200회 반복 = 20,000문장 기준 측정 결과입니다.

### Java vs Python 비교

| API | Java (µs/문장) | Python (µs/문장) | Python/Java |
|-----|---------------|-----------------|-------------|
| analyze | 26.8 | 22.3 | 1.20x 빠름 |
| tokens | 25.7 | 22.5 | 1.14x 빠름 |
| nouns | 20.9 | 22.5 | 0.93x |
| pos | 20.8 | 23.6 | 0.88x |

### 배치 처리 (멀티스레드)

| 방식 | µs/문장 | Java 대비 |
|------|---------|-----------|
| Python 개별 호출 | 22.3 | 1.20x |
| Rust batch (rayon) | 4.1 | **6.57x** |

배치 API를 사용하면 멀티코어를 활용하여 Java 단일스레드 대비 약 6~7배 빠릅니다.
