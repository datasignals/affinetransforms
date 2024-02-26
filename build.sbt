name := "affinetransforms"
version := "0.0.1"
scalaVersion := "2.13.12"
organization := "ch.epfl.scala"

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0"

// Add library dependencies
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0"
libraryDependencies += "org.bouncycastle" % "bcprov-jdk15on" % "1.70"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
libraryDependencies += "com.lihaoyi" %% "utest" % "0.8.2" % "test"

testFrameworks += new TestFramework("utest.runner.Framework")

javaOptions += "-Djava.library.path=/Users/og_pixel/workspace/transforms/blumamba-splitter-native/src/native/build"

mainClass := Some("com.datasignals.affinetransforms.Main")
