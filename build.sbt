lazy val commonSettings = Seq(
  version := "0.1.0",
  organization := "me.maciejb.etcdclient",
  scalaVersion := "2.11.8"
)

val akkaVersion = "2.4.3"
val scalaTestVersion = "2.2.5"
val mockitoVersion = "1.10.19"


lazy val client = project.in(file(".")).
  settings(commonSettings ++ Seq(
    name := "etcd-client",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
      "org.mockito" % "mockito-core" % mockitoVersion % "test",
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test"
    )
  ))
