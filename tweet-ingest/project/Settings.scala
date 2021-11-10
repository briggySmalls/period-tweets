import sbt._
import sbt.librarymanagement.Configurations.Test

object Dependencies {
  val twitter4s = Seq(
    "com.danielasfregola" %% "twitter4s" % Versions.twitter4s
  )

  val elastic4s = Seq(
    "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % Versions.elastic4s,
    "com.sksamuel.elastic4s" %% "elastic4s-testkit" % Versions.elastic4s % "test"
  )

  val akka = Seq(
    "com.typesafe.akka" %% "akka-actor" % Versions.akka,
    "com.typesafe.akka" %% "akka-stream" % Versions.akka,
    "com.typesafe.akka" %% "akka-testkit" % Versions.akka % Test,
  )
}

object Versions {
  val elastic4s = "7.15.1"
  val twitter4s = "7.0"
  val akka = "2.6.16"
}
