# ping-pong example [![Build Status](https://travis-ci.org/daggerok/akka-examples.svg?branch=master)](https://travis-ci.org/daggerok/akka-examples)
A simple akka getting started example quickly created in idea IDE (sbt wrapper, scala 2.13, akka 2.6.x + testkit, scalatest, scalactic)

_test_ and _run_ app:

```bash
./sctw test
./sctw clean run
```

_build_ fat jar:

```bash
./sbtw clean assembly
# ...
java -jar ./target/scala-*/*-assembly-*.jar
```

resources:

* [sbt-assembly plugin](https://github.com/sbt/sbt-assembly)
* [Creating Scala Fat Jars on SBT with sbt-assembly Plugin](http://queirozf.com/entries/creating-scala-fat-jars-for-spark-on-sbt-with-sbt-assembly-plugin)
