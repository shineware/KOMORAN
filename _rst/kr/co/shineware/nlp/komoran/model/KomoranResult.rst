.. java:import:: kr.co.shineware.nlp.komoran.constant SYMBOL

.. java:import:: kr.co.shineware.nlp.komoran.core.model LatticeNode

.. java:import:: kr.co.shineware.nlp.komoran.parser KoreanUnitParser

.. java:import:: kr.co.shineware.util.common.model Pair

KomoranResult
=============

.. java:package:: kr.co.shineware.nlp.komoran.model
   :noindex:

.. java:type:: public class KomoranResult

   Komoran을 통해 분석된 결과를 저장하고 있는 객체입니다.

Constructors
------------

.. java:constructor:: public KomoranResult(List<LatticeNode> latticeNode, String jasoUnits)
   :outertype: KomoranResult

   KomoranResult 생성자 입니다.

   Komoran 내부에서 사용되며 대부분의 경우에 외부에서 사용되지 않습니다.

   :param latticeNode:
   :param jasoUnits:

Methods
-------

.. java:method:: public String getJasoUnits()
   :outertype: KomoranResult

   형태소 분석의 입력 문장을 jaso 단위로 반환합니다.

   :return: jaso 단위로 변환된 String

.. java:method:: public List<Pair<String, String>> getList()
   :outertype: KomoranResult

   분석 결과를 형태소, 품사 Pair의 List 형태로 반환합니다.

   :return: 형태소, 품사 정보가 담긴 Pair의 List

.. java:method:: public List<String> getMorphesByTags(String... str)
   :outertype: KomoranResult

   분석 결과 중 원하는 품사에 해당하는 형태소만 추출하여 반환합니다.

   :param str: 추출 대상 품사
   :return: 품사에 해당하는 형태소만 추출된 List

.. java:method:: public List<String> getMorphesByTags(Collection<String> targetPosCollection)
   :outertype: KomoranResult

   분석 결과 중 원하는 품사에 해당하는 형태소만 추출하여 반환합니다.

   :param targetPosCollection: 추출 대상 품사가 담긴 List
   :return: 품사에 해당하는 형태소만 추출된 List

.. java:method:: public List<String> getNouns()
   :outertype: KomoranResult

   분석 결과 중 명사류(NNG, NNP)만 반환합니다.

   :return: NNG, NNP에 해당하는 형태소가 포함된 List

.. java:method:: public String getPlainText()
   :outertype: KomoranResult

   형태소 분석 결과를 plainText 형태로 반환합니다.

   plainText 결과는 아래와 같습니다.

   .. parsed-literal::

      감기/NNG 는/JX 자주/MAG

   :return: 형태소 분석 결과의 plainText String

.. java:method:: public List<LatticeNode> getResultNodeList()
   :outertype: KomoranResult

   분석 결과를 LatticeNode 리스트로 반환합니다.

   :return: 각 형태소의 LatticeNode List

.. java:method:: public List<Token> getTokenList()
   :outertype: KomoranResult

   형태소 분석 결과를 Token List 형태소 반환합니다.

   Token에는 아래와 같은 정보가 포함되어 있습니다.

   .. parsed-literal::

      private String morph; //형태소
         private String pos; //품사
         private int beginIndex; //입력 문장 내 시작 위치
         private int endIndex; //입력 문장 내 끝 위치

   :return: 형태소 분석 결과의 Token List

