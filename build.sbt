name := """play-c100k-server"""

inThisBuild(Seq(
  version := "0.1.1-PLAY26",
  scalaVersion := "2.11.7",
  resolvers ++= commonResolvers,
  libraryDependencies ++= commonDependencies
))

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, PlayNettyServer, Cinnamon)
  .settings(Seq(
    cinnamon in run := true
  ))

lazy val client = (project in file("client"))
  .enablePlugins(PlayScala, Cinnamon)
  .settings(Seq(
    name := "play-c100k-client",
    cinnamon in run := true
  ))

lazy val commonResolvers = Seq(
  "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
)

lazy val commonDependencies = Seq(
  "com.lightbend.cinnamon" %% "cinnamon-chmetrics-jvm-metrics" % "2.4.0",
  Cinnamon.library.cinnamonAkka,
  Cinnamon.library.cinnamonCHMetrics,
  Cinnamon.library.cinnamonCHMetricsStatsDReporter,
  guice
)
