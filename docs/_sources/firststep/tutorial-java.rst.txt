.. KOMORANDocs documentation master file, created by
   sphinx-quickstart on Sun Mar 17 22:19:31 2019.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Gradle/Maven 없이 형태소 분석 따라하기
===========================================================

이 문서에서는 KOMORAN을 이용한 간단한 형태소 분석을 해보도록 하겠습니다.
만약 ``Gradle`` 이나 ``Maven`` 같은 프로젝트 관리도구를 사용하고 있다면,
:doc:`tutorial` 문서를 참고해주세요.

.. Note::
   문서의 내용 중 지원되지 않거나 잘못된 내용을 발견하실 경우,
   `KOMORAN 문서 프로젝트에 이슈 <https://github.com/shineware/KOMORANDocs/issues>`_ 를 남겨주세요.

----

프로젝트 시작하기
---------------------------------------
프로젝트 관리도구 없이, 직접 Jar 파일을 추가하여 간단한 형태소 분석을 해보겠습니다.
전체 프로젝트 구조는 다음과 같습니다. ::

  .
  ├── App.java
  └── libs
      └── KOMORAN-3.3.4.jar


의존성 추가하기
---------------------------------------
:doc:`installation` 의 ``Jar 파일 만들기`` 부분을 참고하여 Jar 파일을 생성합니다.
생성한 Jar 파일은 ``libs`` 디렉토리를 만든 후, 그 안에 위치시킵니다.

.. Note::
  직접 생성한 Jar 파일의 이름은 ``KOMORAN.jar`` 이지만, 여기서는 관리를 위해 뒤에 버전을 추가하여
  ``KOMORAN-3.3.4.jar`` 로 파일명을 변경하였습니다.


형태소 분석하기
---------------------------------------
이제 Java 클래스를 하나 만들어 형태소 분석을 해보도록 하겠습니다. 여기에서는 별도 패키지 없이,
바로 ``App.java`` 파일을 만들고 ``App`` 클래스를 추가하였습니다.

``App.java`` 파일의 전체 코드는 다음과 같습니다.

.. code-block:: java
  :linenos:
  :emphasize-lines: 10,13,17,19

  import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
  import kr.co.shineware.nlp.komoran.core.Komoran;
  import kr.co.shineware.nlp.komoran.model.KomoranResult;
  import kr.co.shineware.nlp.komoran.model.Token;

  import java.util.List;

  public class App {
    public static void main(String[] args){
      Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
      String strToAnalyze = "대한민국은 민주공화국이다.";

      KomoranResult analyzeResultList = komoran.analyze(strToAnalyze);

      System.out.println(analyzeResultList.getPlainText());

      List<Token> tokenList = analyzeResultList.getTokenList();
      for (Token token : tokenList) {
          System.out.format("(%2d, %2d) %s/%s\n", token.getBeginIndex(), token.getEndIndex(), token.getMorph(), token.getPos());
      }
    }
  }


10번째 줄에서는 FULL 모델을 갖는 ``Komoran`` 객체를 선언하였습니다. 모델의 종류와 설명에 대해서는 이 문서를 참고해주세요.

.. todo::
  FULL / LIGHT 모델의 차이를 설명한 문서를 작성하고, 링크합니다.

13번째 줄에서 생성한 ``Komoran`` 객체의 ``analyze()`` 메소드의 인자로 분석할 문장을 전달하고, 그 결과를
``KomoranResult`` 객체로 저장합니다. ``KomoranResult`` 객체는 분석 결과를 보여주는 몇 가지 메소드들을 갖고
있는데, 여기서는 그 중 2가지를 살펴보겠습니다.

첫번째는 형태소 분석 결과가 태깅된 문장 형태를 받아보는 것으로, ``getPlainText()`` 메소드를 호출하면 됩니다.
15번째 줄에서는 이러한 결과를 바로 출력하고 있습니다.

두번째 결과는 각 형태소(``Token``)를 원소로 갖는 목록(List)으로 받는 것으로, ``getTokenList()`` 메소드를
호출하면 됩니다. ``Token`` 은 형태소와 품사, 그리고 시작/끝 지점을 갖는 객체로, KOMORAN에서 사용하는 모델입니다.

.. todo::
  KOMORAN에서 사용하는 다양한 모델(Tag, Token 및 KomoranResult 등)에 대한 문서를 추가한 후, 링크합니다.

20번째 줄에서는 각 형태소별 시작/끝 지점 및 형태소와 품사를 형식에 맞춰 줄력하고 있습니다.


실행하기
---------------------------------------
``App.java`` 파일이 위치한 곳에서 다음과 같이 Java 파일을 Class 파일로 컴파일합니다.::

  javac -cp "./libs/KOMORAN-3.3.4.jar:" App.java

위 명령어는 ``libs`` 디렉토리 내에 있는 ``KOMORAN-3.3.4.jar`` 파일을 포함하여 ``App.java`` 를 컴파일하도록 하는 명령어입니다.
이제, ``App.class`` 파일이 생성된 것을 확인하실 수 있는데요, 이 파일을 실행해보겠습니다.::

  java -cp ".:./libs/KOMORAN-3.3.4.jar:" App

다음과 같이 실행 결과를 볼 수 있습니다. ::

  대한민국/NNP 은/JX 민주공화국/NNP 이/VCP 다/EF ./SF
  ( 0,  4) 대한민국/NNP
  ( 4,  5) 은/JX
  ( 6, 11) 민주공화국/NNP
  (11, 12) 이/VCP
  (12, 13) 다/EF
  (13, 14) ./SF


결론
---------------------------------------
지금까지 Gradle이나 Maven과 같은 프로젝트 관리도구 없이 의존성을 추가하고, KOMORAN을 이용하여 형태소 분석을 하는 간단한
예제를 살펴보았습니다. GitHub 저장소에서 `Java 버전 <https://github.com/shineware/tutorials/blob/master/KOMORAN/bootstrap-java>`_
의 전체 코드를 확인하실 수 있습니다.
