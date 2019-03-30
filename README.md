# (WIP) KOMORAN Admin

* 사전 관리 도구인 KOMORAN Admin은 현재 개발 진행 중입니다.
* 필요한 기능이 있으신가요? [기능 추가 요청](https://github.com/shineware/KOMORANAdmin/issues/new?template=FEATURE_REQUEST.md)을 남겨주세요!

## 개요

* KOMORANAdmin은 [한국어 형태소 분석기 KOMORAN](https://github.com/shin285/KOMORAN)의 사전들을 관리하는 도구입니다.
* node.js로 만들어진 [DictAdmin](https://github.com/9bow/DictAdmin)과 Frontend를 공유합니다.

## 사용법
### 환경

* JDK 1.8
* Chrome Browser

### 설치

* 개발 중인 KOMORAN Admin을 설치하시려면 다음과 같이 진행해주세요.
* Git을 이용하여 komoran/admin 저장소를 복제합니다.

```sh
  git clone https://github.com/shineware/KOMORANAdmin
  cd KOMORANAdmin
  git checkout development
```

* `./gradlew bootRun`을 실행합니다.

```sh
  ./gradlew bootRun
```

* Chrome Browser를 열고 [http://localhost:8888/](http://localhost:8888/dicword.html) 에 접속합니다.
