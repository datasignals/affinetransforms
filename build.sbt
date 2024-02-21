name := "affinetransforms"
version := "0.0.1"
scalaVersion := "2.13.12"
organization := "ch.epfl.scala"

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0"

// Add library dependencies
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.3.0"
libraryDependencies += "org.bouncycastle" % "bcprov-jdk15on" % "1.70"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"

mainClass := Some("com.datasignals.affinetransforms.Main")
