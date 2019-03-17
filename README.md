
## KOMORAN 3.0
[![Build Status](https://travis-ci.org/shin285/KOMORAN.svg?branch=master)](https://travis-ci.org/shin285/KOMORAN)
[![Coverage Status](https://coveralls.io/repos/github/shin285/KOMORAN/badge.svg?branch=master)](https://coveralls.io/github/shin285/KOMORAN?branch=master)

[English](README.md) | [한국어](README.ko.md)

**KOMORAN** is a **KO**rean **MOR**phological **AN**alyzer implemented in Java.

### Key Features

* Pure Java
  * Because of implemented in ONLY Java, it can be used in any environment where Java is installed.
* Dependency Free
  * There is no dependency issue with external libraries because of self-made libraries.
* Lightweight
  * It need low memory(approx. 50MB) for running, using self made TRIE dictionary and so on.
* Easy to Use
  * Just type a few lines of code for analyzing sentences.
* Easy to manage dictionary
  * Easily edit dictionaries as human-readable plain text file.
* New analysis results
  * Unlike other morpheme analyzers, it is possible to analyze morpheme units containing spaces.

### Demo & Example

* On the [KOMORAN website](http://www.shineware.co.kr/products/komoran/#demo?utm_source=komoran-kr&utm_medium=Referral&utm_campaign=github-demo), you can check the analysis result as below.
* Input sentence: *대한민국은 민주공화국이다.*
![KOMORAN Demo](https://docs.komoran.kr/_images/KOMORAN_Sample_01.png)

### Installation

Please refer to the '[Installation](https://docs.komoran.kr/firststep/installation.html?utm_source=komoran-repo&utm_medium=Referral&utm_campaign=github-demo)' document.

### Quick Start

Please refer to the '[Morphological Analysis Blitz in 3 minutes](https://docs.komoran.kr/firststep/tutorial.html?utm_source=komoran-repo&utm_medium=Referral&utm_campaign=github-demo)' document.

### Examples of use

* [Analysis example]((https://docs.komoran.kr/examples/analyze.html?utm_source=komoran-repo&utm_medium=Referral&utm_campaign=github-demo))
* [Model training example](https://docs.komoran.kr/examples/train-model.html?utm_source=komoran-repo&utm_medium=Referral&utm_campaign=github-demo)
* [Spark2 analysis example (in Scala)](https://docs.komoran.kr/examples/spark2-scala.html?utm_source=komoran-repo&utm_medium=Referral&utm_campaign=github-demo)

### Resources by KOMORAN

* [A demo page](https://www.shineware.co.kr/products/komoran/#demo?utm_source=komoran-kr&utm_medium=Referral&utm_campaign=github-demo) is available to test the performance of KOMORAN.
* You can refer to [KOMORAN official document site](https://docs.komoran.kr?utm_source=komoran-repo&utm_medium=Referral&utm_campaign=github-demo) and how use KOMORAN
* Please visit [KOMORAN Slack](https://komoran.slack.com/join/shared_invite/MTc3NTMzMDQ1NTY5LTE0OTM4MjE5MzktNDE3NmQ4NDNkNw) to share usage and tips.

### Resources by Users

* In addition, there is a [Simple API Server](/9bow/KOMORANRestAPIServer) repo that can be run on yourself.
* There is a repository implemented in Python3 by [Hyunjoong Kim](lovit) - KOMORAN3Py(/lovit/komoran3py)

### Citation

```
@misc{komoran,
author = {Junsoo Shin},
title = {komoran},
publisher = {GitHub},
journal = {GitHub repository},
howpublished = {\url{https://github.com/shin285/KOMORAN}}
```
