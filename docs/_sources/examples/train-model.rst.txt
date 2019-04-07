.. KOMORANDocs documentation master file, created by
   sphinx-quickstart on Thu Mar 14 00:21:42 2019.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

모델 학습 예제
=======================================

이 문서에서는 KOMORAN을 이용한 형태소 분석 예제를 살펴보겠습니다.

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
다음과 같은 예시 코드를 사용하여 모델을 학습할 수 있습니다.

.. code-block:: java
  :linenos:

  package kr.co.shineware.nlp.komoran.test;

  import kr.co.shineware.nlp.komoran.modeler.builder.ModelBuilder;

  import java.io.File;

  public class ModelBuildTest {

    public static void main(String[] args) {
      modelSave();
      modelLoad();
    }

    private static void modelLoad() {
      ModelBuilder builder = new ModelBuilder();
      builder.load("models");
    }

    private static void modelSave() {
      ModelBuilder builder = new ModelBuilder();
      //external dictionary for out of vocabulary
      builder.setExternalDic("user_data"+ File.separator+"wiki.titles");
      //training corpus path must include dictionary, grammar and irregular dictionary
      builder.buildPath("corpus_build");
      //path to save models
      builder.save("models");
    }

  }


.. todo::
  학습에 필요한 데이터 및 모델 학습 결과, 모델 사용 방법 등에 대한 내용 추가가 필요합니다.
