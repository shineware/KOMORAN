.. java:import:: kr.co.shineware.nlp.komoran.constant DEFAULT_MODEL

.. java:import:: kr.co.shineware.nlp.komoran.constant FILENAME

.. java:import:: kr.co.shineware.nlp.komoran.constant SCORE

.. java:import:: kr.co.shineware.nlp.komoran.constant SYMBOL

.. java:import:: kr.co.shineware.nlp.komoran.core.model ContinuousSymbolBuffer

.. java:import:: kr.co.shineware.nlp.komoran.core.model Lattice

.. java:import:: kr.co.shineware.nlp.komoran.core.model LatticeNode

.. java:import:: kr.co.shineware.nlp.komoran.core.model Resources

.. java:import:: kr.co.shineware.nlp.komoran.corpus.parser CorpusParser

.. java:import:: kr.co.shineware.nlp.komoran.corpus.parser.model ProblemAnswerPair

.. java:import:: kr.co.shineware.nlp.komoran.model KomoranResult

.. java:import:: kr.co.shineware.nlp.komoran.model MorphTag

.. java:import:: kr.co.shineware.nlp.komoran.model ScoredTag

.. java:import:: kr.co.shineware.nlp.komoran.modeler.model IrregularNode

.. java:import:: kr.co.shineware.nlp.komoran.modeler.model Observation

.. java:import:: kr.co.shineware.nlp.komoran.parser KoreanUnitParser

.. java:import:: kr.co.shineware.nlp.komoran.util KomoranCallable

.. java:import:: kr.co.shineware.util.common.file FileUtil

.. java:import:: kr.co.shineware.util.common.model Pair

.. java:import:: kr.co.shineware.util.common.string StringUtil

.. java:import:: java.util.concurrent Executors

.. java:import:: java.util.concurrent Future

.. java:import:: java.util.concurrent ThreadPoolExecutor

Komoran
=======

.. java:package:: kr.co.shineware.nlp.komoran.core
   :noindex:

.. java:type:: public class Komoran implements Cloneable

   KOMORAN core 클래스입니다.

Constructors
------------
Komoran
^^^^^^^

.. java:constructor:: public Komoran(String modelPath)
   :outertype: Komoran

   modelPath 디렉토리에 있는 모델 파일들을 로딩하여 객체를 생성합니다.

   modelPath 디렉토리에는 pos.table, observation.model, transition.model, irregular.model 파일이 포함되어 있어야 합니다. 각 파일은 ModelBuilder를 통해 생성됩니다.

   :param modelPath: 모델 파일들이 포함되어 있는 디렉토리 경로

Komoran
^^^^^^^

.. java:constructor:: public Komoran(DEFAULT_MODEL modelType)
   :outertype: Komoran

   Komoran에서 기본으로 제공되는 모델을 로딩하여 객체를 생성합니다.

   별도의 경로를 지정할 필요가 없습니다.

   :param modelType: 기본으로 제공되는 모델의 타입

Methods
-------
analyze
^^^^^^^

.. java:method:: public List<KomoranResult> analyze(List<String> sentences, int thread)
   :outertype: Komoran

   여러 문장을 입력 받아 형태소 분석을 진행합니다.

   :param sentences: 분석할 문장들이 담긴 List. 각 원소는 하나의 문장이라고 간주합니다.
   :param thread: 분석 시 사용할 thread 수
   :return: 문장 별 형태소 분석 결과가 담긴 List

analyze
^^^^^^^

.. java:method:: public KomoranResult analyze(String sentence)
   :outertype: Komoran

   입력된 문장에 대해서 형태소 분석을 진행합니다.

   :param sentence: 분석 대상 문장
   :return: 형태소 분석 결과

analyze
^^^^^^^

.. java:method:: public List<KomoranResult> analyze(String sentence, int nbest)
   :outertype: Komoran

   입력된 문장에 대해서 형태소 분석을 진행 후 n-best 결과를 반환합니다.

   :param sentence: 분석 대상 문장
   :param nbest: 분석 결과 중 추출할 상위 n개의 수
   :return: 형태소 분석 결과 중 nbest 수 만큼의 결과

analyzeTextFile
^^^^^^^^^^^^^^^

.. java:method:: public void analyzeTextFile(String inputFilename, String outputFilename, int thread)
   :outertype: Komoran

   파일 단위로 형태소 분석을 진행합니다.

   :param inputFilename: 분석할 파일 경로
   :param outputFilename: 분석 결과가 저장될 파일 경로
   :param thread: 분석 시 사용할 thread 수

setFWDic
^^^^^^^^

.. java:method:: public void setFWDic(String filename)
   :outertype: Komoran

   형태소 분석 시 사용될 기분석 사전을 로드합니다.

   형태소 분석 진행 전에 로드되어야 합니다.

   .. parsed-literal::

      Komoran komoran = new Komoran(DEFAULT_MODEL.LIGHT);
      komoran.setFWDic("user_data/fwd.user");
      KomoranResult komoranResult = komoran.analyze("감기는 자주 걸리는 병이다");

   :param filename: 기분석 사전 파일 경로

setUserDic
^^^^^^^^^^

.. java:method:: public void setUserDic(String userDic)
   :outertype: Komoran

   형태소 분석 시 사용될 사용자 사전을 로드합니다.

   형태소 분석 진행 전에 로드되어야 합니다.

   .. parsed-literal::

      Komoran komoran = new Komoran(DEFAULT_MODEL.LIGHT);
      komoran.setUserDic("user_date/dic.user");
      KomoranResult komoranResult = komoran.analyze("바람과 함께 사라지다를 봤어");

   :param userDic: 사용자 사전 파일 경로

