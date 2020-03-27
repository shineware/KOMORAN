## KOMORAN 3.0
![Java CI](https://github.com/shineware/KOMORAN/workflows/Java%20CI/badge.svg)
[![Coverage Status](https://coveralls.io/repos/github/shin285/KOMORAN/badge.svg?branch=master)](https://coveralls.io/github/shin285/KOMORAN?branch=master)
![Downloads](https://jitpack.io/v/shin285/KOMORAN/month.svg)
![Downloads](https://jitpack.io/v/shin285/KOMORAN/week.svg)


[English](README.md) | [한국어](README.ko.md)

**KOMORAN**은 **KO**rean **MOR**phological **AN**alyzer의 약자로, Java로 구현한 한국어 형태소 분석기입니다.

### 주요 특징

* 순수한 Java로 구현
  * 100% Java로만 개발되었기 때문에 자바가 설치된 환경이라면 어디서든지 사용 가능합니다.
* 외부 라이브러리 독립적
  * 자체 제작한 Library들만을 사용하여 외부 Library와의 의존성 문제가 없습니다.
* 경량화
  * 자소 단위 처리, TRIE 사전 등으로 약 50MB 메모리 상에서도 동작 가능합니다.
* 쉬운 사용법
  * Library 적용 후 소스 코드 내 1줄만 추가하여 형태소 분석기를 사용할 수 있습니다.
* 사전 관리 용이
  * 일반 텍스트 파일의 형태로 구성되어 가독성이 높으며 바로 편집이 가능합니다.
* 새로운 분석 결과
  * 타 형태소 분석기와 달리 공백이 포함된 형태소 단위로 분석이 가능합니다.

### 데모 및 예시

* [KOMORAN 사이트](http://www.shineware.co.kr/products/komoran/#demo?utm_source=komoran-kr&utm_medium=Referral&utm_campaign=github-demo)에서 아래와 같이 분석 결과를 미리 확인해볼 수 있습니다.
* 입력 문장: 대한민국은 민주공화국이다.
![KOMORAN Demo#1](https://docs.komoran.kr/_images/KOMORAN_Sample_01.png)

### 설치

'[설치하기](https://docs.komoran.kr/firststep/installation.html?utm_source=komoran-repo&utm_medium=Referral&utm_campaign=github-demo)' 문서를 참고해주세요.

### 빠른 사용법

'[3분 만에 형태소 분석 따라하기](https://docs.komoran.kr/firststep/tutorial.html?utm_source=komoran-repo&utm_medium=Referral&utm_campaign=github-demo)' 문서를 참고해주세요.

### 사용 예시

* [분석 예시](https://docs.komoran.kr/examples/analyze.html?utm_source=komoran-repo&utm_medium=Referral&utm_campaign=github-demo)
* [모델 학습 예시](https://docs.komoran.kr/examples/train-model.html?utm_source=komoran-repo&utm_medium=Referral&utm_campaign=github-demo)
* [Spark2 분석 예시 (Scala)](https://docs.komoran.kr/examples/spark2-scala.html?utm_source=komoran-repo&utm_medium=Referral&utm_campaign=github-demo)

### KOMORAN 참고 자료

KOMORAN을 개발한 shineware에서 제공하는 참고자료입니다.

* [shineware 홈페이지](https://www.shineware.co.kr)에서 [KOMORAN 소개 및 데모](https://www.shineware.co.kr/products/komoran/#demo?utm_source=komoran-kr&utm_medium=Referral&utm_campaign=github-demo)를 확인하실 수 있습니다.
* [KOMORAN 공식 문서](https://docs.komoran.kr?utm_source=komoran-repo&utm_medium=Referral&utm_campaign=github-demo)에서 KOMORAN 설치 및 사용 방법 등을 참고하실 수 있습니다
* [KOMORAN Slack](https://komoran.slack.com/join/shared_invite/MTc3NTMzMDQ1NTY5LTE0OTM4MjE5MzktNDE3NmQ4NDNkNw)에 방문하셔서 사용법과 팁 등을 공유해주세요.

### 공식 Wrapper 자료

shineware에서 개발한 공식 wrapper 자료입니다.
* [PyKOMORAN](https://github.com/shineware/PyKOMORAN)에서 Python용 KOMORAN을 사용하실 수 있습니다.

### 그 외 참고 자료

사용자 분들께서 만들어주신 참고자료입니다.

* [9bow](/9bow)님께서 간단히 실행해볼 수 있는 [Simple API Server](/9bow/KOMORANRestAPIServer)을 공개해주셨습니다.
* [Hyunjoong Kim](lovit)님께서 Python 버전의 KOMORAN3Py(/lovit/komoran3py)를 공개해주셨습니다.
* 사전 관리 도구

### 인용

```
@misc{komoran,
author = {Junsoo Shin, Junghwan Park, Geunho Lee},
title = {komoran},
publisher = {GitHub},
journal = {GitHub repository},
howpublished = {\url{https://github.com/shin285/KOMORAN}}
```

### Related Publication
* Ihm, S. Y., Lee, J. H., & Park, Y. H. (2019). Skip-gram-KR: Korean Word Embedding for Semantic Clustering. IEEE Access. (IF: 3.557)

* Kwon, S., Ko, Y., & Seo, J. (2019). Effective vector representation for the Korean named-entity recognition. Pattern Recognition Letters, 117, 52-57. (IF: 1.952)

* Song, H. J., Choi, J. E., Lee, Y. K., Yoon, J. H., Kim, J. D., Park, C. Y., & Kim, Y. S. (2019). A Web Service for Evaluating the Level of Speech in Korean. Applied Sciences, 9(3), 594. (IF: 1.689)

* Han, K., Shim, H., & Yi, M. Y. (2018). A New Biomedical Passage Retrieval Framework for Laboratory Medicine: Leveraging Domain-specific Ontology, Multilevel PRF, and Negation Differential Weighting. Journal of healthcare engineering, 2018. (IF: 1.261)

* Edmiston, D., & Stratos, K. (2018). Compositional Morpheme Embeddings with Affixes as Functions and Stems as Arguments. In Proceedings of the Workshop on the Relevance of Linguistic Structure in Neural Architectures for NLP (pp. 1-5).
