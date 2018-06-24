name := """play-scala-starter-example"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.6"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies += guice
libraryDependencies += jdbc
libraryDependencies += evolutions

libraryDependencies += "org.playframework.anorm" %% "anorm" % "2.6.1"
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.2"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test