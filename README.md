
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
