package fastdata

import java.util.Properties

import akka.actor.ActorSystem
import edu.stanford.nlp.ling.CoreAnnotations
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization.write
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.ejml.simple.SimpleMatrix
import twitter4j._
import twitter4j.conf.ConfigurationBuilder

import scala.collection.mutable
import scala.collection.convert.wrapAll._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn

case class TweetSentence(text: String, index: Int, matrix: Seq[Double])
case class TweetInfo(id: Long, keyword: String, text: String, tags: Seq[String], sentiment: Double, weighted: Double, sentences: Seq[TweetSentence])

object Program {

  implicit val system: ActorSystem = ActorSystem("Twitter-Sentiment")
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  implicit val formats: DefaultFormats.type = DefaultFormats

  def getEnvironmentOrDefault(name: String, default: String) : String = if (sys.env.contains(name)) sys.env(name) else default

  def getTwitterStream(keywords: String, listenerFactory: () => StatusListener) : TwitterStream = {
    val clientId = getEnvironmentOrDefault("TWITTER_CLIENT_ID", "")
    val clientSecret = getEnvironmentOrDefault("TWITTER_CLIENT_SECRET", "")
    val accessToken = getEnvironmentOrDefault("TWITTER_ACCESS_TOKEN", "")
    val accessTokenSecret = getEnvironmentOrDefault("TWITTER_ACCESS_TOKEN_SECRET", "")

    val config = new ConfigurationBuilder()
      .setOAuthConsumerKey(clientId)
      .setOAuthConsumerSecret(clientSecret)
      .setOAuthAccessToken(accessToken)
      .setOAuthAccessTokenSecret(accessTokenSecret)
      .build

    val filter = new FilterQuery(keywords.split(","): _*)
    val stream = new TwitterStreamFactory(config).getInstance
    val listener = listenerFactory()
    stream.addListener(listener)
    stream.filter(filter)
    stream
  }

  def getKafkaProducer() : KafkaProducer[String, String] = {
    val kafkaBootstrapServer = getEnvironmentOrDefault("KAFKA_BOOTSTRAP_SERVER", "kafka:9092")

    val properties = new Properties()
    properties.put("bootstrap.servers", kafkaBootstrapServer)
    properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](properties)
    producer
  }

  def main(args: Array[String]) : Unit = {
    sys.env.foreach(println)

    val keywords = getEnvironmentOrDefault("TWITTER_KEYWORDS", "")
    val topic = getEnvironmentOrDefault("KAFKA_TOPIC", "sentiment")
    val producer = getKafkaProducer()

    val nlp = {
      val properties = new Properties()
      properties.put("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment")
      new StanfordCoreNLP(properties, true)
    }

    val factory = () => new StatusListener {
      override def onStatus(status: Status): Unit = {
        println(status.getId.toString + ": " + status.getText)
        if (!status.isRetweet && status.getLang.equalsIgnoreCase("en")) {
          val future = Future {
            val text = status.getText
              .replace("@[a-zA-Z0-9_]*", "")
              .replace("#", "")
            val annotated: Annotation = nlp.process(text)
            val sentences = annotated.get(classOf[CoreAnnotations.SentencesAnnotation])
            val sentiment = sentences
              .map { s => (s, s.get(classOf[SentimentCoreAnnotations.SentimentAnnotatedTree])) }
              .map { case (s, t) => (
                s.get(classOf[CoreAnnotations.TextAnnotation]),
                RNNCoreAnnotations.getPredictedClass(t),
                RNNCoreAnnotations.getPredictions(t)
              )}

            sentiment
          }

          future.onSuccess {
            case x: mutable.Buffer[(String, Int, SimpleMatrix)] => {
              val sentences = x.map { case (text, index, matrix) => TweetSentence(text, index, Range(0, 5).map { i => matrix.get(i) } ) }
              val info = TweetInfo(
                status.getId,
                keywords,
                status.getText,
                status.getHashtagEntities.map { h => h.getText },
                sentences.map { s => s.matrix(s.index) * (s.index - 2) }.sum / sentences.length,
                sentences.map { s => Range(0, 5).map { i => s.matrix(i) * (i-2) }.sum }.sum / sentences.length,
                sentences
              )
              val json = write(info)
              val record = new ProducerRecord(topic, status.getId.toString, json)
              producer.send(record)
            }
          }
        }
      }
      override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {}
      override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {}
      override def onScrubGeo(userId: Long, upToStatusId: Long): Unit = {}
      override def onStallWarning(warning: StallWarning): Unit = {}
      override def onException(ex: Exception): Unit = { println(ex.toString) }
    }

    val stream = getTwitterStream(keywords, factory)
    println(s"Waiting for tweets for keyword ${keywords}")
    sys.addShutdownHook {
      println("Shutting down")
      stream.cleanUp()
      producer.close()
    }
  }

}

