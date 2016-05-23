organization := "com.github.jw3"
name := "pigpio-scala-examples"
version := "0.1"

scalaVersion := "2.11.8"
scalacOptions += "-target:jvm-1.8"

resolvers += "jw3 at bintray" at "https://dl.bintray.com/jw3/maven"

libraryDependencies ++= {
  val akkaVersion = "2.4.4"
  val scalatestVersion = "3.0.0-M15"

  Seq(
    "com.rxthings" %% "webhooks" % "0.5",
    "com.github.jw3" %% "pigpio-scala" % "0.1-SNAPSHOT",

    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,

    "ch.qos.logback" % "logback-classic" % "1.1.7" % Runtime,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % Runtime,

    "org.scalactic" %% "scalactic" % scalatestVersion % Test,
    "org.scalatest" %% "scalatest" % scalatestVersion % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
  )
}

enablePlugins(JavaAppPackaging)
dockerRepository := Some("jwiii")
dockerBaseImage := "jwiii/pigpio"
