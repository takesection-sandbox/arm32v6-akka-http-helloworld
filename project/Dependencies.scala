import sbt._

object Dependencies {
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http"   % "10.1.0-RC1"
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % "2.5.8" // or whatever the latest version is
}