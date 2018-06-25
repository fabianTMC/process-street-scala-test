name := """play-scala-starter-example"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.6"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies += guice
libraryDependencies += jdbc
libraryDependencies += jdbc % Test
libraryDependencies += evolutions

libraryDependencies += "com.h2database" % "h2" % "1.4.197"
libraryDependencies += "org.playframework.anorm" %% "anorm" % "2.6.1"
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.2"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"