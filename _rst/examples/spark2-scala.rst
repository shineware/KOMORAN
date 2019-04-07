.. KOMORANDocs documentation master file, created by
   sphinx-quickstart on Thu Mar 14 00:21:42 2019.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Spark2 분석 예제 (Scala)
=======================================

이 문서에서는 Spark2에서 KOMORAN을 이용한 분석 예제를 살펴보겠습니다.

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

.. code-block:: scala
  :linenos:

  import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL
  import kr.co.shineware.nlp.komoran.core.Komoran
  import org.apache.spark.sql.SparkSession
  import org.apache.spark.sql.expressions.UserDefinedFunction
  import org.apache.spark.sql.functions.udf

  import scala.collection.JavaConverters._

  object Main {
    val komoran = new Komoran(DEFAULT_MODEL.LIGHT)

    val getPlainTextUdf: UserDefinedFunction = udf[String, String] { sentence =>
      komoran.analyze(sentence).getPlainText
    }

    val getNounsUdf: UserDefinedFunction = udf[Seq[String], String] { sentence =>
      komoran.analyze(sentence).getNouns.asScala
    }

    val getTokenListUdf: UserDefinedFunction = udf[Seq[String], String] { sentence =>
      komoran.analyze(sentence).getTokenList.asScala.map(x => x.toString)
    }

    def main(args: Array[String]): Unit = {
      val spark = SparkSession.builder().enableHiveSupport().getOrCreate()
      import spark.implicits._

      val testDataset = spark.createDataFrame(Seq(
        "밀리언 달러 베이비랑 바람과 함께 사라지다랑 뭐가 더 재밌었어?",
        "아버지가방에들어가신다",
        "나는 밥을 먹는다",
        "하늘을 나는 자동차",
        "아이폰 기다리다 지쳐 애플공홈에서 언락폰질러버렸다 6+ 128기가실버ㅋ"
      ).map(Tuple1.apply)).toDF("sentence")

      // 1. print test data
      testDataset.show(truncate = false)

      val analyzedDataset =
        testDataset.withColumn("plain_text", getPlainTextUdf($"sentence"))
            .withColumn("nouns", getNounsUdf($"sentence"))
            .withColumn("token_list", getTokenListUdf($"sentence"))

      // 2. print test data and analyzed result as list
      analyzedDataset.select("sentence", "token_list").show()

      // 3. print test data and morphes with selected pos
      analyzedDataset.select("sentence", "nouns").show()

      // 4. print test data and analyzed result as pos-tagged text
      analyzedDataset.select("sentence", "plain_text").show()
    }
  }


분석 결과
---------------------------------------
위 코드를 실행한 결과는 다음과 같습니다.

.. code-block:: java
  :linenos:

  +---------------------------------------+
  |sentence                               |
  +---------------------------------------+
  |밀리언 달러 베이비랑 바람과 함께 사라지다랑 뭐가 더 재밌었어?    |
  |아버지가방에들어가신다                            |
  |나는 밥을 먹는다                              |
  |하늘을 나는 자동차                             |
  |아이폰 기다리다 지쳐 애플공홈에서 언락폰질러버렸다 6+ 128기가실버ㅋ|
  +---------------------------------------+

  +--------------------+--------------------+
  |            sentence|          token_list|
  +--------------------+--------------------+
  |밀리언 달러 베이비랑 바람과 함...|[Token [morph=밀리,...|
  |         아버지가방에들어가신다|[Token [morph=아버지...|
  |           나는 밥을 먹는다|[Token [morph=나, ...|
  |          하늘을 나는 자동차|[Token [morph=하늘,...|
  |아이폰 기다리다 지쳐 애플공홈에...|[Token [morph=아이,...|
  +--------------------+--------------------+

  +--------------------+--------------------+
  |            sentence|               nouns|
  +--------------------+--------------------+
  |밀리언 달러 베이비랑 바람과 함...|           [베이비, 바람]|
  |         아버지가방에들어가신다|           [아버지, 가방]|
  |           나는 밥을 먹는다|                 [밥]|
  |          하늘을 나는 자동차|           [하늘, 자동차]|
  |아이폰 기다리다 지쳐 애플공홈에...|[아이, 폰, 애플, 공, 홈,...|
  +--------------------+--------------------+

  +--------------------+--------------------+
  |            sentence|          plain_text|
  +--------------------+--------------------+
  |밀리언 달러 베이비랑 바람과 함...|밀리/VV 어/EC ㄴ/JX 달...|
  |         아버지가방에들어가신다|아버지/NNG 가방/NNG 에/...|
  |           나는 밥을 먹는다|나/NP 는/JX 밥/NNG 을...|
  |          하늘을 나는 자동차|하늘/NNG 을/JKO 나/NP...|
  |아이폰 기다리다 지쳐 애플공홈에...|아이/NNG 폰/NNP 기다리/...|
  +--------------------+--------------------+


.. todo::
  사용 가능한 API 문서를 작성하고, 링크합니다.
