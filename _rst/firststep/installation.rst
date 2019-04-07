.. KOMORANDocs documentation master file, created by
   sphinx-quickstart on Tue Feb 26 22:28:06 2019.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

설치하기
=======================================

이 문서에서는 KOMORAN을 개발 프로젝트에 포함하거나 다운로드받는 방법을 살펴보겠습니다.

.. Note::
   문서의 내용 중 지원되지 않거나 잘못된 내용을 발견하실 경우,
   `KOMORAN 문서 프로젝트에 이슈 <https://github.com/shineware/KOMORANDocs/issues>`_ 를 남겨주세요.

----

환경 준비
---------------------------------------

Java
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
KOMORAN은 Java 8을 기준으로 개발 및 배포되었습니다. Java 8 이전 버전을 사용하고 계시는 경우 업그레이드해주시기 바랍니다.
사용하시는 Java 버전은 다음과 같이 확인하실 수 있습니다. ::

   java -version

또는, `Oracle Java 홈페이지 <https://www.oracle.com/technetwork/java/javase/overview/index.html>`_ 에서 최신 버전의 Java를
설치하실 수 있습니다.

.. Note::
   OpenJDK에서의 동작은 별다른 문제점이 보고되지 않았습니다.
   OpenJDK 사용 중 이슈가 발생한다면 `알려주시기를 <https://github.com/shin285/KOMORAN/issues>`_ 부탁드립니다.

운영체제
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
KOMORAN은 JVM이 설치된 환경이라면 별도로 운영체제를 가리지는 않습니다. 다만, 이 문서에서는 macOS를 기준으로 설명합니다.

Git 도구 또는 개발 환경
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
KOMORAN 다운로드를 위해 Git 도구를 준비해주시기 바랍니다.
또는 IntelliJ나 Eclipse 등과 같은 통합 개발 환경(IDE)을 사용하신다면 해당 도구를 통해서 다운로드 받으실 수도 있습니다.


프로젝트 관리도구 사용
---------------------------------------
maven이나 gradle과 같은 프로젝트 관리 도구를 이용하고 계시다면, 다음과 같은 방법으로 프로젝트에 KOMORAN을 포함하실 수
있습니다. KOMORAN은 `jitpack <https://jitpack.io/>`_ 을 이용하여 패키지를 제공하고 있습니다.

.. Note::
   KOMORAN 최신 버전은 `GitHub 저장소의 배포 메뉴 <https://github.com/shin285/KOMORAN/releases>`_ 에서 확인하실 수 있습니다.

Maven 이용하기
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
먼저, ``pom.xml`` 파일 내에 다음과 같이 저장소(repository)를 추가합니다. 이미 ``repositories`` 항목이 존재한다면,
``<repository>...</repository>`` 부분만 추가합니다. ::

  <repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
  </repositories>

이후, 다음과 같이 의존성(dependency)을 추가합니다. ``pom.xml`` 파일 내에 이미 ``dependencies`` 항목이
존재한다면 ``<dependency>...</dependency>`` 부분만 추가합니다. 다음은 ``3.3.4`` 버전을 추가하는 예시입니다. ::

  <dependencies>
    <dependency>
      <groupId>com.github.shin285</groupId>
      <artifactId>KOMORAN</artifactId>
      <version>3.3.4</version>
    </dependency>
  </dependencies>

이렇게 구성된 `pom.xml 파일 <https://github.com/shineware/tutorials/blob/master/KOMORAN/bootstrap-maven/pom.xml>`_ 을
확인해보실 수 있습니다.

Gradle 이용하기
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
먼저, ``build.gradle`` 파일 내에 다음과 같이 저장소(repository)를 추가합니다. 이미 ``repositories`` 항목이 존재한다면
``maven ...`` 부분만 추가합니다. ::

  allprojects {
    repositories {
      ...
      maven { url 'https://jitpack.io' }
    }
  }

이후, 다음과 같이 의존성(dependency)을 추가합니다. ``build.gradle`` 파일 내에 이미 ``dependencies`` 항목이 존재한다면
``implementation ...`` 부분만 추가합니다. 다음은 ``3.3.4`` 버전을 추가하는 예시입니다. ::

  dependencies {
    implementation 'com.github.shin285:KOMORAN:3.3.4'
  }

이렇게 구성된 `build.gralde 파일 <https://github.com/shineware/tutorials/blob/master/KOMORAN/bootstrap-gradle/build.gradle>`_ 을
확인해보실 수 있습니다.

sbt 이용하기
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
먼저, ``build.sbt`` 파일 내에 다음과 같이 저장소(resolver)를 추가합니다. ::

  resolvers += "jitpack" at "https://jitpack.io"

이후, 다음과 같이 의존성(dependency)을 추가합니다. 다음은 ``3.3.4`` 버전을 추가하는 예시입니다. ::

  libraryDependencies += "com.github.shin285" % "KOMORAN" % "3.3.4"


소스코드 다운로드
---------------------------------------
KOMORAN 소스코드를 분석하시거나, 수동으로 KOMORAN을 프로젝트에 포함하시고자 할 때 사용하시기를 권장하는 방법입니다.
꾸준한 업그레이드를 위해 Git 도구를 이용하여 GitHub 저장소에서 복제받는 방법을 권장합니다. ::

   git clone https://github.com/shin285/KOMORAN

다운로드 후에는 해당 디렉토리 내에서 `git pull` 명령어를 통해 최신 소스를 받아올 수 있습니다. ::

   cd KOMORAN
   git pull

.. seealso::
  Git 도구 홈페이지에서 `사용법 <https://git-scm.com/book/ko/>`_ 을 익히실 수 있습니다.

.. Note::
  특정 버전의 KOMORAN을 다운로드 받으실 때는 `GitHub 저장소의 배포 메뉴 <https://github.com/shin285/KOMORAN/releases>`_
  를 이용하시면 버전별로 압축된 소스코드를 다운로드 받으실 수 있습니다.


Jar 파일 만들기
---------------------------------------
수동으로 KOMORAN을 프로젝트에 포함하거나, 기타 다른 용도로 KOMORAN의 Jar 파일이 필요할 때 사용하시기를 권장하는 방법입니다.
`소스코드 다운로드`_ 후, KOMORAN을 다운로드 받은 디렉토리 내에서 다음과 같은 명령어를 실행합니다.

  ./gradlew jar

.. Note::
  윈도우를 사용하고 계실 경우 다음의 명령어를 실행해주세요.
  ``gradlew.bat jar``

이제, ``build/libs`` 디렉토리 내에서 ``KOMORAN.jar`` 파일을 확인하실 수 있습니다.
