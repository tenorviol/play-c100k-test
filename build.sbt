name := """play-connection-test"""

version := "0.1.0-PLAY26"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, PlayNettyServer, Cinnamon)

scalaVersion := "2.11.7"

cinnamon in run := true

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-simple" % "1.7.25",
  Cinnamon.library.cinnamonCHMetrics,
  Cinnamon.library.cinnamonAkka,
  guice
)
