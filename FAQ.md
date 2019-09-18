# 자주 묻는 질문과 답변 (FAQ)

## 형태소 분석기 관련 FAQ

* **질문(이하 Q)**: KOMORAN은 무엇인가요?
  * **답변(이하 A)**: [KOMORAN](https://github.com/shin285/KOMORAN)은 [신준수](https://github.com/shin285)님께서 [2013년](https://shineware.tistory.com/entry/KOMORAN-ver-05-자바-한글-형태소-분석기)부터 개발해온 한국어 형태소 분석기입니다. 더 자세한 내용은 [홈페이지](https://www.shineware.co.kr/products/komoran/?utm_source=komoran-admin&utm_medium=Referral&utm_campaign=admin-github)를 참고해주세요.

* **Q**: KOMORAN 외에 다른 형태소 분석기도 사용할 수 있나요?
  * **A**: KOMORAN 관리도구는 KOMORAN을 위해 개발되어 다른 형태소 분석기에서는 사용이 어렵습니다. 하지만, 사전의 형태가 동일하다면 사전 보정을 위해서는 사용할 수 있습니다.

## 사전 관련 FAQ

* **Q**: 사전 데이터는 어디에 저장되나요?
  * **A**: 사전 데이터는 H2 Database에 저장되며, AdminDB.mv.db라는 파일로 존재합니다. SQL을 사용할 줄 아신다면 [H2 콘솔](http://localhost:3579/console)에서도 살펴보실 수 있습니다.

* **Q**: 사전 데이터를 파일로 받고 싶어요.
  * **A**: 개별 사전을 파일 형태로 다운로드 받으시려면 각 사전 보정 메뉴 상단의 '내려받기'를 이용해주세요.

* **Q**: 형태소 분석 결과 비교를 위해 임시로 일부 단어를 사전에서 제외하고 싶습니다. 삭제 / 등록 외에 방법이 있나요?
  * **A**: 사용자 사전과 기분석 사전은 단어가 `#` 문자로 시작하는 경우 해당 단어를 형태소 분석 시에 인식하지 않습니다. 단어 빈도와 품사 빈도에서는 사용할 수 없는 기능이니 참고해주세요.

* **Q**: 사전 파일들은 어떤 형식으로 되어 있나요?
  * **A**: 각 사전 파일들의 형식은 다음과 같습니다.

  | 사전명                   | 파일 규칙                            | 예시(행)                        | 예시(파일)                                                                           | 비고                          |
  | ------------------------ | ------------------------------------ | ------------------------------- | ------------------------------------------------------------------------------------ | ----------------------------- |
  | 단어 빈도 (`dic.word`)   | `단어\t품사:빈도`                    | `감사원	NNG:40`                 | [예시 파일](https://github.com/shin285/KOMORAN/blob/master/corpus_build/dic.word)    | -                             |
  | 품사 빈도 (`grammar.in`) | `이전품사\t다음품사1:빈도,다음품사2:빈도,...` | `VCN	EF:1874,EP:785,EC:4037` | [예시 파일](https://github.com/shin285/KOMORAN/blob/master/corpus_build/grammar.in) | -                        |
  | 사용자 사전 (`dic.user`) | `단어\t품사`                         | `바람과 함께	NNG`              | [예시 파일](https://github.com/shin285/KOMORAN/blob/master/user_data/dic.user)       | 단어가 `#`으로 시작 시 무시함 |
  | 기분석 사전 (`fwd.user`) | `기분석어절\t형태소1/품사1 형태소2/품사2 ...` | `감기는	감/NNG 기는/NNG`    | [예시 파일](https://github.com/shin285/KOMORAN/blob/master/user_data/fwd.user) | 기분석 어절이 `#`으로 시작 시 무시함 |

## 품사 관련 FAQ

* **Q**: 품사의 종류는 어떻게 되나요? 어떤 품사들을 사용하나요?
  * **A**: KOMORAN 형태소 분석기와 KOMORAN 관리도구는 `21세기 세종계획`의 품사 기준을 따르고 있습니다. 개별 품사들의 목록은 [KOMORAN 문서](http://docs.komoran.kr/?utm_source=komoran-admin&utm_medium=Referral&utm_campaign=admin-github)의 [품사표(PoS Table)](http://docs.komoran.kr/firststep/postypes.html?utm_source=komoran-admin&utm_medium=Referral&utm_campaign=admin-github)을 참고해주세요.

* **Q**: 품사 빈도에는 품사표에 없는 품사가 있습니다. 어떤 품사인가요?
  * **A**: 품사 빈도에는 품사표에 존재하는 품사들 외에 어절의 시작(`BOE`)과 끝(`EOE`)을 표시하는 별도의 품사가 존재합니다.

## 모델 관련 FAQ

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
  └── model               # 위 corpus_build/ 디렉토리 내의 데이터로 생성한 사용자 모델
      ├── irregular.model
      ├── observation.model
      ├── pos.table
      └── transition.model
  ````

* **Q**: 모델 생성 시 사전을 확인하라는 메시지와 함께 모델 생성이 실패합니다.
  * **A**: 모델 생성 시 실패 원인은 사전 데이터에 있을 확률이 높습니다. 특히 품사 사전에 빠트린 품사 쌍이 있는지 확인해주세요.

## 기타 사용 관련 FAQ

* **Q**: 모델 생성 시간이 다르게 표시됩니다.
  * **A**: 모델 생성 시의 시간은 시스템 시간을 사용합니다. 모델 생성 시간이 다르게 표시된다면 KOMORAN 관리도구가 실행 중인 시스템의 시간을 확인해주세요.

## 실행 환경 및 설정 관련 FAQ

* **Q**: 다른 컴퓨터에서도 KOMORAN 관리도구에 접근할 수 있게 하고 싶습니다.
  * **A**: application.yaml 파일의 2번째줄의 `address: 127.0.0.1`을 `address: 0.0.0.0`으로 변경하면 외부에서도 접속이 가능합니다.

* **Q**: Oracle JDK 외에 다른 JDK도 지원하나요?
  * **A**: 네, Oracle Java 1.8 외에 OpenJDK 1.8에서도 정상적으로 실행이 가능한 것을 확인하였습니다. 만약 사용 중 문제가 발생한다면 이슈를 남겨주세요.

* **Q**: 최신 버전의 Java도 지원하나요?
  * **A**: 2019년 9월 현재 Oracle Java 12에서 정상적으로 실행이 가능한 것을 확인하였습니다. 만약 사용 중 문제가 발생한다면 이슈를 남겨주세요.

* **Q**: 1GB 이하의 적은 메모리에서도 사용이 가능한가요?
  * **A**: 다음과 같이 설정하여 512MB의 가용 메모리에서 실행 가능한 것을 확인하였습니다. (단, Gradle Daemon은 별도입니다.)

* **Q**: 메모리가 부족하여 메모리 사용량을 512MB로 제한하고 싶습니다.
  * **A**: gralde.properties 파일의 첫 번째 줄의 설정을 다음과 같이 변경해주세요. `org.gradle.jvmargs=-Xmx256m -Xmx3g -XX:MaxPermSize=256m -Dfile.encoding=UTF-8`

## KOMORAN 관리도구 활용 관련 FAQ

* **Q**: KOMORAN 관리도구를 회사에서 사용하고 싶습니다.
  * **A**: KOMORAN 관리도구는 Apache License 2.0으로 배포되고 있습니다. 따라서 절차 없이 영리 또는 비영리 목적으로 사용하실 수 있습니다.

* **Q**: KOMORAN 관리도구의 소스코드를 변경하였습니다. 반드시 공개해야 하나요?
  * **A**: 변경한 코드는 공개하지 않으셔도 됩니다. 하지만 다른 분들께도 필요한 내용이라면 [Pull Request](https://github.com/shineware/KOMORANAdmin/pulls)를 보내주시기를 부탁드립니다.

* **Q**: KOMORAN 관리도구를 개선하는데 기여하고 싶습니다.
  * **A**: 감사합니다! KOMORAN 관리도구 저장소에 [Pull Request](https://github.com/shineware/KOMORANAdmin/pulls)를 보내주세요.
