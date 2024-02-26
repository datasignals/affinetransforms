name := "affinetransforms"
version := "0.0.1"
scalaVersion := "2.13.12"
organization := "ch.epfl.scala"

// Add library dependencies
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0"
libraryDependencies += "org.bouncycastle" % "bcprov-jdk15on" % "1.70"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
libraryDependencies += "com.lihaoyi" %% "utest" % "0.8.2" % "test"

testFrameworks += new TestFramework("utest.runner.Framework")

javaOptions += "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED java.library.path=/Users/og_pixel/workspace/transforms/blumamba-splitter-native/src/native/build"
Compile / test / javaOptions += "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED -Djava.library.path=/Users/og_pixel/workspace/transforms/blumamba-splitter-native/src/native/build"
fork := true

mainClass := Some("com.datasignals.affinetransforms.Main")

enablePlugins(JavaAppPackaging)

mappings in Universal ++= Seq(
  file("/Users/og_pixel/workspace/transforms/blumamba-splitter-native/src/native/build") -> "lib"
)

