# KOMORAN Release Notes

## v4.0.0

### 성능 벤치마크

`stress.test` 20,000문장 기준 측정 결과:

| 항목 | v3.x | v4.0.0 | 개선율 |
|------|------|--------|--------|
| 단일 스레드 | 706ms | 545ms | **22.8% 향상** |
| 멀티 스레드 (4T) | 221ms | 172ms | **22.2% 향상** |
| 초당 처리량 (1T) | ~28,300 문장/초 | ~36,700 문장/초 | +30% |
| 초당 처리량 (4T) | ~90,500 문장/초 | ~116,300 문장/초 | +28% |
| 메모리 사용량 | ~130MB | ~95MB | **27% 절감** |

> 측정 환경: OpenJDK 17 (Corretto), Apple Silicon, 5회 반복 평균 (warmup 1회 제외)

---

### 핵심 성능 최적화

#### 1. 배열 기반 Aho-Corasick 오토마톤

사전 탐색의 핵심 자료구조를 **HashMap 기반 트라이**에서 **배열 기반 Double-Array 오토마톤**으로 교체했습니다.

| 항목 | v3.x | v4.0.0 |
|------|------|--------|
| 자료구조 | HashMap 트라이 | 연속 배열 (BFS 순서) |
| 노드 접근 | 포인터 체이싱 | 배열 인덱스 직접 접근 |
| 자식 탐색 | HashMap.get() | 이진 탐색 (정렬 배열) |
| 메모리 레이아웃 | 비연속적 (힙 분산) | 연속적 (캐시 라인 친화) |
| GC 부담 | HashMap Entry 객체 다수 | 원시 int 배열 |

BFS 순서로 노드를 배치하여 탐색 시 인접 메모리를 순차 접근하게 되므로, CPU 캐시 히트율이 크게 향상됩니다.

#### 2. JasoEncoder - Compact 자소 인코딩

유니코드 전체 범위(65,536)를 실제 사용되는 자소/문자 약 **400개**로 압축 인코딩합니다.

```
호환 자모 (ㄱ~ㅎ, ㅏ~ㅣ):  51자
ASCII printable:            95자
한글 자모 확장:             256자
→ 총 알파벳 크기: ~400 (164배 축소)
```

Aho-Corasick 오토마톤의 알파벳 크기가 줄어들어 자식 배열 크기와 탐색 범위가 함께 감소합니다.

#### 3. Lattice 전이 확인 단일화

Viterbi 경로 탐색에서 가장 빈번하게 호출되는 전이 확인 로직을 최적화했습니다.

```java
// v3.x: 이중 메서드 호출
if (transition.hasTransition(prevTagId, tagId)) {        // 배열 접근 1회
    double score = transition.getScore(prevTagId, tagId); // 배열 접근 2회 (중복)
}

// v4.0.0: 단일 호출
double score = transition.getScore(prevTagId, tagId);     // 배열 접근 1회
if (score == Double.NEGATIVE_INFINITY) continue;
```

- 전이 행렬 접근 횟수 50% 감소
- Double 박싱/언박싱 제거 (primitive double 직접 반환)

#### 4. 품사 비교 정수화

품사(POS) 비교를 문자열 비교에서 정수 비교로 변경했습니다.

```java
// v3.x: String.equals() — hashCode 계산 + 문자 단위 비교
if (node.getTag().equals("EOE")) { ... }

// v4.0.0: int 비교 — CPU 단일 사이클
if (prevTagId == SEJONGTAGS.EOE_ID) { ... }
```

Lattice의 모든 노드 탐색에서 수행되므로, 누적 효과가 큽니다.

#### 5. KomoranResult 음절 조회 O(1)화

분석 결과에서 자소 인덱스 → 음절 위치 변환을 선형 탐색에서 배열 직접 접근으로 변경했습니다.

```java
// v3.x: O(n) 선형 탐색
for (int i = 0; i < syllableAreaList.size(); i++) {
    if (syllableAreaList.get(i).getFirst() <= jasoIdx && ...) return i;
}

// v4.0.0: O(1) 배열 직접 조회
int syllableIdx = beginMap[jasoIdx];
```

#### 6. 기타 최적화

| 항목 | 변경 내용 |
|------|----------|
| KoreanUnitParser | 자모 역방향 조회를 HashMap O(1)으로 변경 |
| HangulJamoUtil | if-else 체인(230줄) → 배열 인덱싱 |
| 문자열 처리 | `"" + char` → `String.valueOf(char)` |
| Map 순회 | `keySet()` + `get()` → `entrySet()` 단일 순회 |
| Transition 모델 | `Double[][]` → `double[][]` primitive 배열 |

---

### 구조 개선

#### 모델 단일화

| 항목 | v3.x | v4.0.0 |
|------|------|--------|
| 모델 구조 | `models_full` + `models_light` 이중 관리 | `models` 단일 디렉토리 |
| 초기화 방식 | `new Komoran(DEFAULT_MODEL.STABLE)` | `new Komoran()` |
| DEFAULT_MODEL enum | STABLE, EXP 등 구분 | 제거 |

```java
// v3.x
Komoran komoran = new Komoran(DEFAULT_MODEL.STABLE);

// v4.0.0
Komoran komoran = new Komoran();
```

#### Dead Code 제거

- `DEFAULT_MODEL` enum 삭제
- deprecated 생성자 제거
- 미사용 메서드(`hasRegularFWDValues`, `insertLattice`) 제거
- 주석 처리된 코드 블록 제거

#### 빌드 환경 현대화

| 항목 | v3.x | v4.0.0 |
|------|------|--------|
| Java | 8+ | **17** |
| Gradle | 7.3.3 | **9.0** |
| 불필요 모듈 | admin, elasticsearch-plugin 포함 | core만 유지 |
| CI/CD | Travis CI + GitHub Actions | 제거 (정리 예정) |
| 문서 | Sphinx RST (2,000+ 파일) | README.md 단일 문서 |

#### 프로젝트 경량화

```
삭제된 파일: 300+ 파일, 200,000+ 라인
- admin/ 모듈 전체
- elasticsearch-plugin/ 모듈 전체
- docs/ (Sphinx 빌드 산출물)
- _rst/ (Sphinx 소스)
- .github/ (이슈 템플릿, PR 템플릿 등)
```

---

### 마이그레이션 가이드

#### API 변경

```java
// v3.x
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
Komoran komoran = new Komoran(DEFAULT_MODEL.STABLE);
Komoran komoran = new Komoran(DEFAULT_MODEL.LIGHT);

// v4.0.0 — 단순히 기본 생성자 사용
Komoran komoran = new Komoran();
```

#### 요구 사항 변경

- Java 8 → **Java 17 이상** 필수
- Gradle 7.x → **Gradle 9.0**

#### 제거된 기능

- `DEFAULT_MODEL` enum (`STABLE`, `LIGHT`, `EXP` 구분 없음)
- Elasticsearch Plugin (별도 프로젝트로 분리 권장)
- 내장 Admin 웹 도구
