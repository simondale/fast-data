package fastdata

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.ActorMaterializer
import com.datastax.driver.core.Cluster
import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import spray.json._

import scala.concurrent.{Future, Promise}
import scala.collection.JavaConverters._
import scala.io.StdIn

case class CurrentKeywordSentiment(keyword: String, sentiment: Double, weighted: Double, count: Int)

object KeywordSentimentFormat extends DefaultJsonProtocol {
  implicit val format = jsonFormat4(CurrentKeywordSentiment)
}

import KeywordSentimentFormat._

object Program {

  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  def getEnvironmentOrDefault(name: String, default: String) : String = if (sys.env.contains(name)) sys.env(name) else default

  def main(args: Array[String]) : Unit = {

    implicit def listenableFutureToFuture[T](listenableFuture: ListenableFuture[T]) : Future[T] = {
      val promise = Promise[T]()
      Futures.addCallback(listenableFuture, new FutureCallback[T] {
        override def onSuccess(result: T): Unit = {
          promise.success(result)
          ()
        }
        override def onFailure(t: Throwable): Unit = {
          promise.failure(t)
          ()
        }
      })
      promise.future
    }

    implicit val session = new Cluster
      .Builder()
      .addContactPoints(getEnvironmentOrDefault("CASSANDRA_SERVER", "cassandra"))
      .withPort(9042)
      .build()
      .connect()

    val statement = session.prepare("SELECT * FROM sentiment.current_keyword_sentiment")

    val routes = {
      pathPrefix("current") {
        get {
          val result = session.executeAsync(statement.bind)
          val mapped = result.map(_.asScala.map(r => CurrentKeywordSentiment(
            r.getString("keyword"),
            r.getDecimal("sentiment").doubleValue,
            r.getDecimal("weighted").doubleValue,
            r.getInt("count")
          )))
          onSuccess(mapped) { m =>
            complete(HttpEntity(ContentTypes.`application/json`, m.toList.toJson.toString))
          }
        }
      }
    }

    val future = Http().bindAndHandle(routes, "0.0.0.0", 5000)
    println("Listening on port 5000")
    sys.addShutdownHook {
      println("Shutting down")
      session.close()
      future.flatMap(_.unbind).onComplete(_ => system.terminate)
    }
  }
}
