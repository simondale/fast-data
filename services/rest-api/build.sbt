name := "rest-api"

version := "0.1"

scalaVersion := "2.11.8"

lazy val akkaVersion = "2.5.12"

resolvers ++= Seq(
  Resolver.bintrayRepo("websudos", "oss-releases")
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.1",
  "com.typesafe.akka" %% "akka-stream" % "2.5.11",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.1",
  "net.liftweb" %% "lift-json" % "2.6.2",
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.3.0"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case x => MergeStrategy.first
}