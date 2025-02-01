name := """ScalaPlay"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.16"

libraryDependencies += guice
libraryDependencies += "com.h2database" % "h2" % "1.4.196"
libraryDependencies += "org.playframework" %% "play-slick" % "6.1.1"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "5.3.1"