# KOMORAN 관리도구

## 소개

* KOMORAN 관리도구는 [한국어 형태소 분석기 KOMORAN](https://github.com/shin285/KOMORAN)의 사용을 돕기 위한 도구입니다.
* KOMORAN의 사용자 모델을 만드는데 필요한 사전들을 관리하고, 해당 사전들이 적용된 사용자 모델을 만들고 적용 방법을 알 수 있습니다.

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
  * `java -version` 명령어로 현재 버전을 확인할 수 있습니다.
* Chrome 브라우저
  * 최신 버전의 Safari, Firefox 등에서도 동작합니다.
* KOMORAN 3.3.5 이상
  * 별도로 설치하실 필요는 없습니다.
  * 단, 본 관리도구에서 생성한 사전 및 모델은 3.3.5 이상 버전에서 사용 가능합니다.

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
  * (처음 실행 시에는 필요한 라이브러리들을 내려받는데 다소 시간이 소요됩니다.)

  ```sh
  # macOS 및 Linux 사용 시 아래와 같이 실행합니다.
  ./gradlew bootRun
  # Windows 사용 시 아래와 같이 실행합니다.
  gradlew.bat bootRun
  # 실행이 완료되면 아래와 같은 메시지가 출력됩니다.
  # ##### KOMORAN Admin has just started. #####
  ```

* Chrome 브라우저를 열고 [http://localhost:3579/](http://localhost:3579/) 에 접속합니다.

## 자주 묻는 질문과 답변

* [자주 묻는 질문과 답변](https://github.com/shineware/KOMORANAdmin/blob/master/FAQ.md) 문서를 참조해주세요.

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
