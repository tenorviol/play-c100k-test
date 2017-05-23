name := """play-connection-test"""

version := "0.1.0-PLAY26"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, PlayNettyServer)

scalaVersion := "2.11.7"
