# KOMORAN 관리도구

## 소개

* KOMORAN 관리도구는 [한국어 형태소 분석기 KOMORAN](https://github.com/shin285/KOMORAN)의 사용을 돕기 위한 도구입니다.
* KOMORAN 사용자 모델을 만드는데 필요한 사전들을 관리하고, 해당 사전들을 적용한 사용자 모델을 만들 수 있습니다.

## 주요 기능

### 메뉴 소개

KOMORAN 관리도구는 크게 4가지 기능을 제공하는 메뉴들로 구성되어 있습니다.

* 분석
  * KOMORAN 기본 모델(`LIGHT`)로 형태소 분석을 할 수 있습니다.
* 보정
  * 사용자 모델을 만드는데 필요한 사전들을 관리합니다.
* 비교
  * 사용자 모델을 생성하고, 기본 모델과 생성한 모델들 중 2가지를 선택하여 형태소 분석 결과를 비교할 수 있습니다.
* 배포
  * 사용자 모델을 생성 / 삭제할 수 있으며, 사용자 모델을 배포할 수 있는 방법을 안내합니다.

### 화면 소개

각 메뉴들은 다음과 같은 화면들로 구성되어 있습니다.

| 분석 메뉴 | 보정 메뉴 |
| --------- | --------- |
| ![분석 메뉴](https://raw.githubusercontent.com/shineware/KOMORANAdmin/master/src/main/resources/static/demo/ScreenShot01_Analyze.png) | ![보정 메뉴](https://raw.githubusercontent.com/shineware/KOMORANAdmin/master/src/main/resources/static/demo/ScreenShot02_ManageDict.png) |

| 비교 메뉴 | 배포 메뉴 |
| --------- | --------- |
| ![비교 메뉴](https://raw.githubusercontent.com/shineware/KOMORANAdmin/master/src/main/resources/static/demo/ScreenShot03_CompareResults.png) | ![배포 메뉴](https://raw.githubusercontent.com/shineware/KOMORANAdmin/master/src/main/resources/static/demo/ScreenShot04_DeployModel.png) |

## 설치 및 실행

### 필요 환경

* Java 1.8 이상
  * `java -version` 으로 버전 확인이 가능합니다.
* Chrome 브라우저
  * 최신 버전의 Safari, Firefox 등에서도 동작합니다.

### 설치 방법

* 아래와 같이 Git을 이용하여 KOMORAN 관리도구를 복제합니다.
  * 또는 [배포 메뉴](https://github.com/shineware/KOMORANAdmin/releases)에서 최신 소스코드를 다운로드 받을 수 있습니다.

```sh
  git clone https://github.com/shineware/KOMORANAdmin
```

### 실행 방법

* 위에서 복제 또는 다운로드받은 소스 코드가 위치하는 디렉토리로 이동합니다.

```sh
  cd KOMORANAdmin
```

* KOMORAN 관리도구를 실행합니다.

```sh
  # macOS 및 Linux 사용 시 아래와 같이 실행합니다.
  ./gradlew bootRun
  # Windows 사용 시 아래와 같이 실행합니다.
  gradlew.bat bootRun
```

* Chrome 브라우저를 열고 [http://localhost:8888/](http://localhost:8888/) 에 접속합니다.

## 자주 묻는 질문과 답변

### KOMORAN 형태소 분석기

* **질문(이하 Q)**: KOMORAN은 무엇인가요?
  * **답변(이하 A)**: [KOMORAN](https://github.com/shin285/KOMORAN)은 [신준수](https://github.com/shin285)님께서 [2013년](https://shineware.tistory.com/entry/KOMORAN-ver-05-자바-한글-형태소-분석기)부터 개발해온 한국어 형태소 분석기입니다. 더 자세한 내용은 [홈페이지](https://www.shineware.co.kr/products/komoran/?utm_source=komoran-kr&utm_medium=Referral&utm_campaign=admin-github)를 참고해주세요.

* **Q**: KOMORAN 외에 다른 형태소 분석기도 사용할 수 있나요?
  * **A**: KOMORAN 관리도구는 KOMORAN을 위해 개발되어 다른 형태소 분석기에서는 사용이 어렵습니다. 하지만, 사전의 형태가 동일하다면 사전 보정을 위해서는 사용할 수 있습니다.

### KOMORAN 관리도구 사용법

* **Q**: 사전 데이터는 어디에 저장되나요?
  * **A**: 사전 데이터는 H2 Database에 저장되며, AdminDB.mv.db라는 파일로 존재합니다. SQL을 사용할 줄 아신다면 [H2 콘솔](http://localhost:8888/console)에서도 살펴보실 수 있습니다.

* **Q**: 사전 데이터를 파일로 받고 싶어요.
  * **A**: 개별 사전을 파일 형태로 다운로드 받으시려면 각 사전 보정 메뉴 상단의 '내려받기'를 이용해주세요.

* **Q**: 모델은 어디에 저장되나요?
  * **A**: 프로젝트 디렉토리 바로 아래에 `models/`라는 이름의 디렉토리 아래에 저장됩니다. 각 하위 디렉토리 하나가 모델 하나를 포함하고 있으며, 디렉토리명이 모델의 생성 시각입니다.

* **Q**: 모델 디렉토리에는 어떤 내용들이 포함되어 있나요?
  * **A**: 모델 디렉토리 내에는 2개의 디렉토리(`corpus_build/`, `model/`)와 2개의 파일(`dic.user`, `fwd.user`)이 존재합니다. 각 내용은 다음과 같습니다.

  ```sh
  .
  ├── corpus_build        # 사용자 모델을 생성하기 위한 기초 데이터
  │   ├── dic.irregular   # 불규칙 사전
  │   ├── dic.word        # 단어 빈도 사전
  │   └── grammar.in      # 품사 빈도 사전
  ├── dic.user            # 사용자 단어 사전
  ├── fwd.user            # 기분석 사전
  └── model               # 위 corpus_build 디렉토리 내의 데이터로 빌드한 사용자 모델 디렉토리
      ├── irregular.model
      ├── observation.model
      ├── pos.table
      └── transition.model
  ````

* **Q**: 사전 파일들은 어떤 형식으로 되어 있나요?
  * **A**: 각 사전 파일들의 형식은 다음과 같습니다.

  | 사전명 | 파일 규칙 | 예시(행) | 예시(파일) | 비고 |
  | ------ | --------- | -------- | ---------- | ---- |
  | 단어 빈도 (`dic.word`) | `단어\t품사:빈도` | `감사원	NNG:40` | [예시 파일](https://github.com/shin285/KOMORAN/blob/master/corpus_build/dic.word) | - |
  | 품사 빈도 (`grammar.in`) | `이전품사\t다음품사1:빈도,다음품사2:빈도,...` | `VCN	EF:1874,EP:785,EC:4037` | [예시 파일](https://github.com/shin285/KOMORAN/blob/master/corpus_build/grammar.in) | - |
  | 사용자 사전 (`dic.user`) | `단어\t품사` | `바람과 함께	NNG` | [예시 파일](https://github.com/shin285/KOMORAN/blob/master/user_data/dic.user) | 단어가 `#`으로 시작 시 무시함 |
  | 기분석 사전 (`fwd.user`) | `기분석어절\t형태소1/품사1 형태소2/품사2 ...` | `감기는	감/NNG 기는/NNG` | [예시 파일](https://github.com/shin285/KOMORAN/blob/master/user_data/fwd.user) | 기분석 어절이 `#`으로 시작 시 무시함 |

* **Q**: 다른 컴퓨터에서도 접근할 수 있나요? 공동 작업에 사용해도 괜찮은가요?
  * **A**: KOMORAN 관리도구는 개인 사용을 전제로 개발한 것으로 별도의 접근 제어를 하지 않습니다. 따라서, 서버 등에서 계속 켜두는 것을 권장하지 않습니다.

## 기여

* KOMORAN 관리도구와 관련한 소스 코드나 버그 리포트, 오타 수정 등 어떠한 기여도 환영합니다.
  * 저장소에 [이슈](https://github.com/shineware/KOMORANAdmin/issues)를 남겨주시거나, [Pull Request](https://github.com/shineware/KOMORANAdmin/pulls)를 보내주세요.
  * [KOMORAN Slack](https://komoran.slack.com/) 또한 열려있습니다. ([초대 링크](http://goo.gl/T1d3Ia))
* 단, 기여해주신 모든 내용은 [Apache License 2.0](https://github.com/shineware/KOMORANAdmin/blob/master/LICENSE)으로 공개되므로, 이에 동의하시는 경우에만 참여해주시기 바랍니다.

## 라이선스

* KOMORAN 및 KOMORAN 관리도구는 [Apache License 2.0](https://github.com/shineware/KOMORANAdmin/blob/master/LICENSE)을 따릅니다.
  * 누구든 KOMORAN 관리도구의 일부 또는 전체를 개인적 또는 상업적 목적으로 사용할 수 있습니다.
  * SHINEWARE 및 이 저장소의 기여자(들)은 KOMORAN 관리도구를 사용하는 것에 대해 어떠한 권리도 주장하지 않습니다.
  * SHINEWARE 및 이 저장소의 기여자(들)은 KOMORAN 관리도구를 사용함에 따라 발생하는 어떠한 책임도 지지 않습니다.
* 단, KOMORAN 관리도구에서 사용한 아래 라이브러리들은 각자의 라이선스를 따릅니다.
  * [Tabulator 3.5](https://github.com/olifolkerd/tabulator/blob/3.5/LICENSE) ([MIT License](https://opensource.org/licenses/MIT))
  * [AdminLTE 2.4.2 및 이에 포함된 라이브러리들](https://adminlte.io/docs/2.4/license) ([MIT License](https://opensource.org/licenses/MIT))
  * [Spring Boot 2.1.2 및 이에 포함된 라이브러리들](https://github.com/spring-projects/spring-boot/blob/2.1.x/LICENSE.txt) ([Apache License 2.0](https://opensource.org/licenses/Apache-2.0))
  * [json-simple 1.1.1](https://code.google.com/archive/p/json-simple/) ([Apache License 2.0](https://opensource.org/licenses/Apache-2.0))
  * [java-diff-utils 4.0](https://github.com/java-diff-utils/java-diff-utils/blob/java-diff-utils-4.0/LICENSE) ([Apache License 2.0](https://opensource.org/licenses/Apache-2.0))
  * [Zip4j 2.1.3](https://github.com/srikanth-lingala/zip4j/blob/v2.1.3/LICENSE) ([Apache License 2.0](https://opensource.org/licenses/Apache-2.0))
  * [Java Hamcrest 1.3](https://github.com/hamcrest/JavaHamcrest/blob/hamcrest-java-1.3/LICENSE.txt) ([BSD License](https://opensource.org/licenses/BSD-3-Clause))
  * [H2 Database](https://www.h2database.com/html/license.html) ([Mozilla Public License 2.0](https://opensource.org/licenses/MPL-2.0) / [Eclipse Public License 1.0](https://opensource.org/licenses/eclipse-1.0.php))