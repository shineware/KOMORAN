# KOMORAN

![GitHub Stars](https://img.shields.io/github/stars/shineware/KOMORAN)
![GitHub Forks](https://img.shields.io/github/forks/shineware/KOMORAN)
![License](https://img.shields.io/github/license/shineware/KOMORAN)
![Java](https://img.shields.io/badge/Java-17%2B-blue)
![Gradle](https://img.shields.io/badge/Gradle-9.0-blue)
![Downloads/month (shin285)](https://jitpack.io/v/shin285/KOMORAN/month.svg)
![Downloads/week (shin285)](https://jitpack.io/v/shin285/KOMORAN/week.svg)
![Downloads/month (shineware)](https://jitpack.io/v/shineware/KOMORAN/month.svg)
![Downloads/week (shineware)](https://jitpack.io/v/shineware/KOMORAN/week.svg)

**KOMORAN**은 **KO**rean **MOR**phological **AN**alyzer의 약자로, Java로 구현한 한국어 형태소 분석기입니다.

## 주요 특징

- **순수 Java 구현** - Java 17 이상이 설치된 환경이라면 어디서든 사용 가능
- **외부 라이브러리 독립적** - 자체 Library만 사용하여 의존성 문제 없음
- **경량화** - 자소 단위 처리, 배열 기반 Aho-Corasick 사전으로 약 95MB 메모리에서 동작
- **멀티스레드 지원** - 대용량 텍스트를 멀티스레드로 병렬 분석 가능
- **사전 관리 용이** - 일반 텍스트 파일 형태로 구성되어 직접 편집 가능
- **공백 포함 형태소 분석** - 타 형태소 분석기와 달리 공백이 포함된 형태소 단위 분석 지원

## 요구 사항

- Java 17 이상
- Gradle 9.0

## 설치

### Gradle

```groovy
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation 'com.github.shineware.KOMORAN:core:master-SNAPSHOT'
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.shineware.KOMORAN</groupId>
    <artifactId>core</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

## 사용법

### 기본 형태소 분석

```java
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;

Komoran komoran = new Komoran();
KomoranResult result = komoran.analyze("대한민국은 민주공화국이다.");

// 형태소/품사 쌍 출력
result.getList().forEach(pair ->
    System.out.println(pair.getFirst() + "\t" + pair.getSecond())
);
// 대한민국  NNP
// 은       JX
// 민주     NNG
// 공화국   NNG
// 이       VCP
// 다       EF
// .        SF
```

### 결과 조회 방법

```java
KomoranResult result = komoran.analyze("네가 없는 거리에는 내가 할 일이 많아서 마냥 걷다보면 추억을 가끔 마주치지.");

// 1. 형태소/품사 쌍 리스트
List<Pair<String, String>> morphList = result.getList();

// 2. 명사만 추출
List<String> nouns = result.getNouns();

// 3. 특정 품사의 형태소만 추출
List<String> verbs = result.getMorphesByTags("VV", "NNG");
List<String> endings = result.getMorphesByTags("EC");

// 4. 분석 결과를 plain text로 출력
String plainText = result.getPlainText();
// "네/NP 가/JKS 없/VA 는/ETM 거리/NNG 에/JKB 는/JX ..."

// 5. Token 리스트 (위치 정보 포함)
List<Token> tokens = result.getTokenList();
for (Token token : tokens) {
    System.out.println(token);  // morph/pos (beginIndex, endIndex)
}
```

### 멀티스레드 분석

대용량 텍스트를 병렬로 처리하여 분석 속도를 높일 수 있습니다.

```java
Komoran komoran = new Komoran();

// 방법 1: 문장 리스트를 멀티스레드로 분석
List<String> sentences = Arrays.asList(
    "대한민국은 민주공화국이다.",
    "대한민국의 주권은 국민에게 있고, 모든 권력은 국민으로부터 나온다.",
    "..."
);
List<KomoranResult> results = komoran.analyze(sentences, 4);  // 4 threads

// 방법 2: 파일 단위 분석 (입력 파일 → 결과 파일)
komoran.analyzeTextFile("input.txt", "output.txt", 4);  // 4 threads
```

### 사용자 사전 적용

```java
Komoran komoran = new Komoran();

// 사용자 사전 설정 (한 줄에 하나의 단어, 탭으로 품사 구분)
// 예: dic.user 파일 내용
//   바람과 함께 사라지다	NNP
//   싸이	NNP
komoran.setUserDic("dic.user");

System.out.println(komoran.analyze("싸이는 가수다").getPlainText());
// 싸이/NNP 는/JX 가수/NNG 다/EF
```

### 기분석 사전 적용

특정 단어에 대해 원하는 분석 결과를 강제할 수 있습니다.

```java
Komoran komoran = new Komoran();

// 기분석 사전 설정 (한 줄에 하나의 규칙, 탭으로 구분)
// 예: fwd.user 파일 내용
//   감기는	감기/NNG 는/JX
komoran.setFWDic("fwd.user");

System.out.println(komoran.analyze("감기는").getTokenList());
// [감기/NNG(0,2), 는/JX(2,3)]
```

## 품사 태그

KOMORAN은 세종 품사 태그셋을 사용합니다.

| 대분류 | 태그 | 설명 |
|-------|------|------|
| 체언 | NNG | 일반 명사 |
| | NNP | 고유 명사 |
| | NNB | 의존 명사 |
| | NP | 대명사 |
| | NR | 수사 |
| 용언 | VV | 동사 |
| | VA | 형용사 |
| | VX | 보조 용언 |
| | VCP | 긍정 지정사 |
| | VCN | 부정 지정사 |
| 관형사 | MM | 관형사 |
| 부사 | MAG | 일반 부사 |
| | MAJ | 접속 부사 |
| 감탄사 | IC | 감탄사 |
| 조사 | JKS | 주격 조사 |
| | JKC | 보격 조사 |
| | JKG | 관형격 조사 |
| | JKO | 목적격 조사 |
| | JKB | 부사격 조사 |
| | JKV | 호격 조사 |
| | JKQ | 인용격 조사 |
| | JX | 보조사 |
| | JC | 접속 조사 |
| 어미 | EP | 선어말 어미 |
| | EF | 종결 어미 |
| | EC | 연결 어미 |
| | ETN | 명사형 전성 어미 |
| | ETM | 관형형 전성 어미 |
| 접두사 | XPN | 체언 접두사 |
| 접미사 | XSN | 명사 파생 접미사 |
| | XSV | 동사 파생 접미사 |
| | XSA | 형용사 파생 접미사 |
| 어근 | XR | 어근 |
| 부호 | SF | 마침표, 물음표, 느낌표 |
| | SP | 쉼표, 가운뎃점, 콜론, 빗금 |
| | SS | 따옴표, 괄호표, 줄표 |
| | SE | 줄임표 |
| | SO | 붙임표(물결, 숨김, 빠짐) |
| | SW | 기타 기호 |
| 한글 이외 | SL | 외국어 |
| | SH | 한자 |
| | SN | 숫자 |
| 분석 불능 | NA | 분석 불능 |

## 성능 벤치마크

`stress.test` 20,000문장(실제 한국어 텍스트) 기준 측정 결과입니다.

### 분석 속도

| 항목 | 결과 |
|------|------|
| **단일 스레드** | 20,000문장 / 545ms (문장당 0.027ms) |
| **멀티 스레드 (4 threads)** | 20,000문장 / 172ms (문장당 0.009ms) |

### 스레드 수에 따른 처리량

| 스레드 수 | 총 소요 시간 | 초당 처리 문장 수 |
|----------|-------------|-----------------|
| 1 | 545ms | ~36,700 문장/초 |
| 4 | 172ms | ~116,300 문장/초 |

> 측정 환경: OpenJDK 17 (Corretto), Apple Silicon, 5회 반복 평균 (warmup 1회 제외)

### 메모리 사용량

- 사전 로딩 후 약 **95MB** 수준에서 동작
- 멀티스레드 사용 시에도 사전은 공유되므로 스레드 수에 비례한 메모리 증가 없음

## 빌드

```bash
./gradlew :core:build
```

## 인용

```bibtex
@misc{komoran,
  author = {Junsoo Shin, Junghwan Park, Geunho Lee},
  title = {KOMORAN},
  publisher = {GitHub},
  journal = {GitHub repository},
  howpublished = {\url{https://github.com/shineware/KOMORAN}}
}
```

## 라이선스

Apache License 2.0
