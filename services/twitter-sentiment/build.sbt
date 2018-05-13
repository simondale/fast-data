name := "twitter-sentiment"

version := "0.1"

scalaVersion := "2.11.8"

lazy val akkaVersion = "2.5.12"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "org.apache.kafka" %% "kafka" % "0.10.0.0",
  "org.twitter4j" % "twitter4j-core" % "[4.0,)",
  "org.twitter4j" % "twitter4j-stream" % "[4.0,)",
  "net.liftweb" %% "lift-json" % "2.6.2",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.9.1" artifacts (
    Artifact("stanford-corenlp", "models"),
    Artifact("stanford-corenlp")
  )
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case x => MergeStrategy.first
}