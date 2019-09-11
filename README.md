# KOMORAN 관리도구

## 소개

* KOMORAN 관리도구는 [한국어 형태소 분석기 KOMORAN](https://github.com/shin285/KOMORAN)의 사용을 돕기 위한 도구입니다.
* KOMORAN 사용자 모델을 만드는데 필요한 사전들을 관리하고, 해당 사전들을 적용한 사용자 모델을 만들 수 있습니다.

## 주요 기능

KOMORAN 관리도구는 크게 4가지 기능을 제공하는 메뉴들로 구성되어 있습니다.

* 분석
  * KOMORAN 기본 모델(`LIGHT`)로 형태소 분석을 할 수 있습니다.
* 조정
  * 사용자 모델을 만드는데 필요한 사전들을 관리합니다.
* 비교
  * 사용자 모델을 생성하고, 기본 모델과 생성한 모델들 중 2가지를 선택하여 형태소 분석 결과를 비교할 수 있습니다.
* 배포
  * 사용자 모델을 생성 / 삭제할 수 있으며, 사용자 모델을 배포할 수 있는 방법을 안내합니다.

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
