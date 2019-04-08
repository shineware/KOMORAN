.. java:import:: kr.co.shineware.nlp.komoran.constant FILENAME

.. java:import:: kr.co.shineware.nlp.komoran.constant SYMBOL

.. java:import:: kr.co.shineware.nlp.komoran.corpus.model Dictionary

.. java:import:: kr.co.shineware.nlp.komoran.corpus.model Grammar

.. java:import:: kr.co.shineware.nlp.komoran.corpus.parser CorpusParser

.. java:import:: kr.co.shineware.nlp.komoran.corpus.parser IrregularParser

.. java:import:: kr.co.shineware.nlp.komoran.corpus.parser.model ProblemAnswerPair

.. java:import:: kr.co.shineware.nlp.komoran.exception FileFormatException

.. java:import:: kr.co.shineware.nlp.komoran.interfaces UnitParser

.. java:import:: kr.co.shineware.nlp.komoran.parser KoreanUnitParser

.. java:import:: kr.co.shineware.util.common.file FileUtil

.. java:import:: kr.co.shineware.util.common.model Pair

.. java:import:: kr.co.shineware.util.common.string StringUtil

.. java:import:: java.io BufferedReader

.. java:import:: java.io File

.. java:import:: java.io FileReader

.. java:import:: java.lang Character.UnicodeBlock

.. java:import:: java.util ArrayList

.. java:import:: java.util HashSet

.. java:import:: java.util List

.. java:import:: java.util Set

CorpusBuilder
=============

.. java:package:: kr.co.shineware.nlp.komoran.corpus.builder
   :noindex:

.. java:type:: public class CorpusBuilder

   코퍼스로부터 모델 생성 시 필요한 데이터 생성 생성되는 데이터는 아래와 같음 - 단어 사전(word dictionary) - 문법(grammar) - 기분석 사전(full word-phrase dictionary) - 불규칙 사전(irregular dictionary)

   :author: Junsoo Shin

Constructors
------------
CorpusBuilder
^^^^^^^^^^^^^

.. java:constructor:: public CorpusBuilder()
   :outertype: CorpusBuilder

Methods
-------
appendUserDic
^^^^^^^^^^^^^

.. java:method:: public void appendUserDic(String filename)
   :outertype: CorpusBuilder

   filename에 해당하는 사용자 사전을 추가합니다.

   사용자 사전은 코퍼스 빌드 시 함께 빌드됩니다.

   :param filename: 사용자 사전 경로

appendUserDicPath
^^^^^^^^^^^^^^^^^

.. java:method:: public void appendUserDicPath(String path, String suffix)
   :outertype: CorpusBuilder

   path 밑에 있는 모든 파일 중 확장자가 suffix로 끝나는 파일들만 사용자 사전으로 추가합니다.

   추가된 사용자 사전들은 코퍼스 빌드 시 함께 빌드됩니다.

   :param path: 사용자 사전들이 포함된 최상위 디렉토리 경로
   :param suffix: 사용자 사전의 파일확장자

build
^^^^^

.. java:method:: public void build(String filename)
   :outertype: CorpusBuilder

   filename에 해당하는 파일을 빌드합니다.

   :param filename: 빌드 대상 파일 경로

buildPath
^^^^^^^^^

.. java:method:: public void buildPath(String corporaPath)
   :outertype: CorpusBuilder

   coporaPath 밑에 있는 모든 파일을 빌드합니다.

   모든 서브 디렉토리에 있는 파일들도 빌드됩니다.

   :param corporaPath: 빌드 대상 파일들이 포함된 최상위 디렉토리 경로

buildPath
^^^^^^^^^

.. java:method:: public void buildPath(String corporaPath, String suffix)
   :outertype: CorpusBuilder

   coporaPath 밑에 있는 모든 파일 중 파일 확장자가 suffix로 끝나는 파일들만 빌드합니다.

   모든 서브 디렉토리에 있는 파일들도 빌드됩낟.

   :param corporaPath: 빌드 대상 파일들이 포함된 최상위 디렉토리 경로
   :param suffix: 빌드 대상 파일확장자

load
^^^^

.. java:method:: @Deprecated public void load(String loadPath)
   :outertype: CorpusBuilder

save
^^^^

.. java:method:: public void save(String savePathName)
   :outertype: CorpusBuilder

   빌드한 코퍼스를 savePathName 디렉토리에 저장합니다.

   savePathName 디렉토리에는 dic.irregular, dic.word, grammar.in 파일이 저장됩니다.

   :param savePathName: 빌드 데이터가 저장될 디렉토리

setExclusiveIrrRule
^^^^^^^^^^^^^^^^^^^

.. java:method:: public void setExclusiveIrrRule(String filename)
   :outertype: CorpusBuilder

