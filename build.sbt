enablePlugins(GitVersioning, JavaAppPackaging)

organization := "com.github.jw3"
name := "pigpio-scala-examples"
git.useGitDescribe := true

scalaVersion := "2.12.6"
scalacOptions ++= Seq(
  "-encoding", "UTF-8",

  "-feature",
  "-unchecked",
  "-deprecation",

  "-language:postfixOps",
  "-language:implicitConversions",

  "-Ywarn-unused-import",
  "-Xfatal-warnings",
  "-Xlint:_"
)

libraryDependencies ++= {
  lazy val akkaVersion = "2.5.14"
  lazy val akkaHttpVersion = "10.1.3"
  lazy val scalatestVersion = "3.0.3"

  Seq(
    "com.github.jw3" %% "pigpio-scala" % "0.1.1",

    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,

    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",

    "org.scalactic" %% "scalactic" % scalatestVersion % Test,
    "org.scalatest" %% "scalatest" % scalatestVersion % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
  )
}

dockerBaseImage := "jwiii/pigpio"
dockerExposedPorts := Seq(9000)
dockerUpdateLatest := true

