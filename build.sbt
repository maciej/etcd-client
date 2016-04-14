val akkaVersion = "2.4.3"
val scalaTestVersion = "2.2.5"
val mockitoVersion = "1.10.19"


lazy val client = project.in(file("."))
  .settings(name := "etcd-client")
  .settings(Settings.commons ++ Settings.release)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
      "org.mockito" % "mockito-core" % mockitoVersion % "test",
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test"
    )
  )
