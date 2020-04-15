.. KOMORANDocs documentation master file, created by
   sphinx-quickstart on Thu Mar 14 00:21:42 2019.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

분석 예제
=======================================

이 문서에서는 KOMORAN 모델 학습 예제를 살펴보겠습니다.

.. Note::
   문서의 내용 중 지원되지 않거나 잘못된 내용을 발견하실 경우,
   `KOMORAN 문서 프로젝트에 이슈 <https://github.com/shineware/KOMORANDocs/issues>`_ 를 남겨주세요.

----

들어가기
---------------------------------------
KOMORAN을 아직 설치하지 않으셨거나 프로젝트에 포함하는 방법을 모르신다면,
:doc:`../firststep/installation` 또는 :doc:`../firststep/tutorial` 문서를 먼저 참고해주세요.


문장 분석
---------------------------------------
다음과 같은 예시 코드를 사용하여 분석할 수 있습니다.

.. code-block:: java
  :linenos:

  package kr.co.shineware.nlp.komoran.test;

  import kr.co.shineware.nlp.komoran.core.Komoran;
  import kr.co.shineware.nlp.komoran.model.KomoranResult;
  import kr.co.shineware.nlp.komoran.model.Token;

  import java.util.List;

  public class KomoranTest {

    public static void main(String[] args) throws Exception {

      Komoran komoran = new Komoran(DEFAULT_MODEL.LIGHT);
      komoran.setFWDic("user_data/fwd.user");
      komoran.setUserDic("user_data/dic.user");

      String input = "밀리언 달러 베이비랑 바람과 함께 사라지다랑 뭐가 더 재밌었어?";
      KomoranResult analyzeResultList = komoran.analyze(input);
      List<Token> tokenList = analyzeResultList.getTokenList();

      // 1. print each tokens by getTokenList()
      System.out.println("==========print 'getTokenList()'==========");
      for (Token token : tokenList) {
        System.out.println(token);
        System.out.println(token.getMorph()+"/"+token.getPos()+"("+token.getBeginIndex()+","+token.getEndIndex()+")");
        System.out.println();
      }

      // 2. print nouns
      System.out.println("==========print 'getNouns()'==========");
      System.out.println(analyzeResultList.getNouns());
      System.out.println();

      // 3. print analyzed result as pos-tagged text
      System.out.println("==========print 'getPlainText()'==========");
      System.out.println(analyzeResultList.getPlainText());
      System.out.println();

      // 4. print analyzed result as list
      System.out.println("==========print 'getList()'==========");
      System.out.println(analyzeResultList.getList());
      System.out.println();

      // 5. print morphes with selected pos
      System.out.println("==========print 'getMorphesByTags()'==========");
      System.out.println(analyzeResultList.getMorphesByTags("NP", "NNP", "JKB"));
    }
  }


분석 결과
---------------------------------------
위 코드를 실행한 결과는 다음과 같습니다.

.. code-block:: java
  :linenos:

    ==========print 'getTokenList()'==========
    Token [morph=밀리언 달러 베이비, pos=NNP, beginIndex=0, endIndex=10]
    밀리언 달러 베이비/NNP(0,10)

    Token [morph=랑, pos=JKB, beginIndex=10, endIndex=11]
    랑/JKB(10,11)

    Token [morph=바람과 함께 사라지다, pos=NNP, beginIndex=12, endIndex=23]
    바람과 함께 사라지다/NNP(12,23)

    Token [morph=랑, pos=JKB, beginIndex=23, endIndex=24]
    랑/JKB(23,24)

    Token [morph=뭐, pos=NP, beginIndex=25, endIndex=26]
    뭐/NP(25,26)

    Token [morph=가, pos=JKS, beginIndex=26, endIndex=27]
    가/JKS(26,27)

    Token [morph=더, pos=MAG, beginIndex=28, endIndex=29]
    더/MAG(28,29)

    Token [morph=재밌, pos=VA, beginIndex=30, endIndex=32]
    재밌/VA(30,32)

    Token [morph=었, pos=EP, beginIndex=32, endIndex=33]
    었/EP(32,33)

    Token [morph=어, pos=EF, beginIndex=33, endIndex=34]
    어/EF(33,34)

    Token [morph=?, pos=SF, beginIndex=34, endIndex=35]
    ?/SF(34,35)

    ==========print 'getNouns()'==========
    [밀리언 달러 베이비, 바람과 함께 사라지다]

    ==========print 'getPlainText()'==========
    밀리언 달러 베이비/NNP 랑/JKB 바람과 함께 사라지다/NNP 랑/JKB 뭐/NP 가/JKS 더/MAG 재밌/VA 었/EP 어/EF ?/SF

    ==========print 'getList()'==========
    [Pair [first=밀리언 달러 베이비, second=NNP], Pair [first=랑, second=JKB], Pair [first=바람과 함께 사라지다, second=NNP], Pair [first=랑, second=JKB], Pair [first=뭐, second=NP], Pair [first=가, second=JKS], Pair [first=더, second=MAG], Pair [first=재밌, second=VA], Pair [first=었, second=EP], Pair [first=어, second=EF], Pair [first=?, second=SF]]

    ==========print 'getMorphesByTags()'==========
    [밀리언 달러 베이비, 랑, 바람과 함께 사라지다, 랑, 뭐]


.. todo::
  사용 가능한 API 문서를 작성하고, 링크합니다.
