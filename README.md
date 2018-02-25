
# KOMORAN 3.0
[![Build Status](https://travis-ci.org/shin285/KOMORAN.svg?branch=master)](https://travis-ci.org/shin285/KOMORAN)
[![Coverage Status](https://coveralls.io/repos/github/shin285/KOMORAN/badge.svg?branch=master)](https://coveralls.io/github/shin285/KOMORAN?branch=master)

**KO**rean **MOR**phological **AN**alyzer

# Importing the project
This project is based on Gradle with Java.
So, you can open or import this project as Gradle project.  
Due to KOMORAN is published in JitPack, it is also possible to use KOMORAN in sbt project.  

```scala
// In sbt project
resolvers += MavenRepository("jitpack.io", "https://jitpack.io")
libraryDependencies += "com.github.shin285" % "KOMORAN" % "3.3.3"
```

If you don't have IDE can support Gradle project, you can type below command in your console like cmd and terminal for converting as eclipse project after downloading this project.
```shell
./gradlew eclipse
```

# Citation
```markdown
@misc{komoran,
author = {Junsoo Shin},
title = {komoran},
publisher = {GitHub},
journal = {GitHub repository},
howpublished = {\url{https://github.com/shin285/KOMORAN}}
```

# Demo
* A [demo page](http://www.shineware.co.kr/products/komoran/#demo?utm_source=komoran-kr&utm_medium=Referral&utm_campaign=github-demo) is available to test the performance of KOMORAN.
* In addition, there is a [Simple API Server repo](https://github.com/9bow/KOMORANRestAPIServer) that can be run on yourself.


# Usage

### For analyzing

```java
/*******************************************************************************
 * KOMORAN 3.0 - Korean Morphology Analyzer
 *
 * Copyright 2015 Shineware http://www.shineware.co.kr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 	
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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

		//print each tokens by getTokenList()
		System.out.println("==========print 'getTokenList()'==========");
		for (Token token : tokenList) {
			System.out.println(token);
			System.out.println(token.getMorph()+"/"+token.getPos()+"("+token.getBeginIndex()+","+token.getEndIndex()+")");
			System.out.println();
		}
		/*
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
		*/
		
		//print nouns
		System.out.println("==========print 'getNouns()'==========");
		System.out.println(analyzeResultList.getNouns());
		System.out.println();
		/*
		==========print 'getNouns()'==========
		[밀리언 달러 베이비, 바람과 함께 사라지다]
		*/
		System.out.println("==========print 'getPlainText()'==========");
		System.out.println(analyzeResultList.getPlainText());
		System.out.println();
		/*
		==========print 'getPlainText()'==========
		밀리언 달러 베이비/NNP 랑/JKB 바람과 함께 사라지다/NNP 랑/JKB 뭐/NP 가/JKS 더/MAG 재밌/VA 었/EP 어/EF ?/SF
		*/
		System.out.println("==========print 'getList()'==========");
		System.out.println(analyzeResultList.getList());
		System.out.println();
		/*
		==========print 'getList()'==========
		[Pair [first=밀리언 달러 베이비, second=NNP], Pair [first=랑, second=JKB], Pair [first=바람과 함께 사라지다, second=NNP], Pair [first=랑, second=JKB], Pair [first=뭐, second=NP], Pair [first=가, second=JKS], Pair [first=더, second=MAG], Pair [first=재밌, second=VA], Pair [first=었, second=EP], Pair [first=어, second=EF], Pair [first=?, second=SF]]
		*/
		System.out.println("==========print 'getMorphesByTags()'==========");
		System.out.println(analyzeResultList.getMorphesByTags("NP", "NNP", "JKB"));
		/*
		==========print 'getMorphesByTags()'==========
		[밀리언 달러 베이비, 랑, 바람과 함께 사라지다, 랑, 뭐]
		*/
	}
}

```


### For training (model builder)
```java
/*******************************************************************************
 * KOMORAN 3.0 - Korean Morphology Analyzer
 *
 * Copyright 2015 Shineware http://www.shineware.co.kr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 	
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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

```

### Spark2 Scala Example
```scala
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

    testDataset.show(truncate = false)
    //    +---------------------------------------+
    //    |sentence                               |
    //    +---------------------------------------+
    //    |밀리언 달러 베이비랑 바람과 함께 사라지다랑 뭐가 더 재밌었어?    |
    //    |아버지가방에들어가신다                            |
    //    |나는 밥을 먹는다                              |
    //    |하늘을 나는 자동차                             |
    //    |아이폰 기다리다 지쳐 애플공홈에서 언락폰질러버렸다 6+ 128기가실버ㅋ|
    //    +---------------------------------------+

    val analyzedDataset =
      testDataset.withColumn("plain_text", getPlainTextUdf($"sentence"))
          .withColumn("nouns", getNounsUdf($"sentence"))
          .withColumn("token_list", getTokenListUdf($"sentence"))

    analyzedDataset.select("sentence", "token_list").show()
    //    +--------------------+--------------------+
    //    |            sentence|          token_list|
    //    +--------------------+--------------------+
    //    |밀리언 달러 베이비랑 바람과 함...|[Token [morph=밀리,...|
    //    |         아버지가방에들어가신다|[Token [morph=아버지...|
    //    |           나는 밥을 먹는다|[Token [morph=나, ...|
    //    |          하늘을 나는 자동차|[Token [morph=하늘,...|
    //    |아이폰 기다리다 지쳐 애플공홈에...|[Token [morph=아이,...|
    //    +--------------------+--------------------+
    analyzedDataset.select("sentence", "nouns").show()
    //    +--------------------+--------------------+
    //    |            sentence|               nouns|
    //    +--------------------+--------------------+
    //    |밀리언 달러 베이비랑 바람과 함...|           [베이비, 바람]|
    //    |         아버지가방에들어가신다|           [아버지, 가방]|
    //    |           나는 밥을 먹는다|                 [밥]|
    //    |          하늘을 나는 자동차|           [하늘, 자동차]|
    //    |아이폰 기다리다 지쳐 애플공홈에...|[아이, 폰, 애플, 공, 홈,...|
    //    +--------------------+--------------------+
    analyzedDataset.select("sentence", "plain_text").show()
    //    +--------------------+--------------------+
    //    |            sentence|          plain_text|
    //    +--------------------+--------------------+
    //    |밀리언 달러 베이비랑 바람과 함...|밀리/VV 어/EC ㄴ/JX 달...|
    //    |         아버지가방에들어가신다|아버지/NNG 가방/NNG 에/...|
    //    |           나는 밥을 먹는다|나/NP 는/JX 밥/NNG 을...|
    //    |          하늘을 나는 자동차|하늘/NNG 을/JKO 나/NP...|
    //    |아이폰 기다리다 지쳐 애플공홈에...|아이/NNG 폰/NNP 기다리/...|
    //    +--------------------+--------------------+
  }
}
```
