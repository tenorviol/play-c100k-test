name := """play-connection-test"""

version := "0.1.0-PLAY25"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, PlayNettyServer)

scalaVersion := "2.11.7"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

libraryDependencies ++= Seq(
//  guice
)
