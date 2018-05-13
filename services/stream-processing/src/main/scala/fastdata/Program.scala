package fastdata

import com.datastax.driver.core.Cluster
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SaveMode._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{avg, count, from_json, from_unixtime, round}
import org.apache.spark.sql.types.{DoubleType, StringType, StructType}
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}


object Program {

  def getEnvironmentOrDefault(name: String, default: String) : String = if (sys.env.contains(name)) sys.env(name) else default

  def main(args: Array[String]) : Unit = {
    createTables()

    val conf = new SparkConf()
      .setAppName("KafkaStream")
      .set("spark.cassandra.connection.host", getEnvironmentOrDefault("CASSANDRA_SERVER", "cassandra"))

    val sc = new SparkContext(conf)
    sc.setLogLevel("ERROR")

    val ssc = new StreamingContext(sc, Seconds(60))

    val props = Map(
      "bootstrap.servers" -> getEnvironmentOrDefault("KAFKA_BOOTSTRAP_SERVER", "kafka:9092"),
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "auto.offset.reset" -> "earliest",
      "group.id" -> "0",
      "enable.auto.commit" -> (true: java.lang.Boolean)
    )

    val topics = Array("sentiment")
    val stream = KafkaUtils.createDirectStream[String, String](ssc, PreferConsistent, Subscribe[String,String](topics, props))

    val struct = new StructType()
      .add("keyword", StringType)
      .add("sentiment", DoubleType)
      .add("weighted", DoubleType)

    val struct2 = new StructType()
      .add("keyword", StringType)
      .add("text", StringType)

    stream.foreachRDD((rdd, t) => {
      val spark = SparkSession.builder().config(rdd.sparkContext.getConf).getOrCreate()
      import spark.implicits._

      val df = rdd.map(r => (r.key(), r.value(), t.milliseconds / 1000)).toDF
        .select($"_1" as "key", $"_2" as "value", $"_3" as "seconds")
        .withColumn("json", from_json($"value", struct))
        .select(
          from_unixtime($"seconds", "yyyyMMdd") as "day",
          $"seconds" as "timestamp",
          $"key" as "id",
          $"json.keyword" as "keyword",
          $"json.sentiment" as "sentiment",
          $"json.weighted" as "weighted"
        )
        .groupBy($"keyword", $"day", $"timestamp")
        .agg(
          round(avg($"sentiment"), 3) as "sentiment",
          round(avg($"weighted"), 3) as "weighted",
          count($"id") as "count"
        )
        .orderBy($"keyword" asc, $"day" desc)

      df.write.format("org.apache.spark.sql.cassandra").options(Map("table"->"keyword_sentiment", "keyspace"->"sentiment")).mode(Append).save()

      val cur = df.select($"keyword", $"sentiment", $"weighted", $"count")
      cur.write.format("org.apache.spark.sql.cassandra").options(Map("table"->"current_keyword_sentiment", "keyspace"->"sentiment")).mode(Overwrite).save()

      val raw = rdd.map(r => (r.key(), r.value(), t.milliseconds / 1000)).toDF
        .select($"_1" as "key", $"_2" as "value", $"_3" as "seconds")
        .withColumn("json", from_json($"value", struct2))
        .select(
          from_unixtime($"seconds", "yyyyMMdd") as "day",
          from_unixtime($"seconds") as "timestamp",
          $"key" as "id",
          $"json.keyword" as "keyword",
          $"json.text" as "text"
        )

      raw.write.format("org.apache.spark.sql.cassandra").options(Map("table"->"tweet_text", "keyspace"->"sentiment")).mode(Append).save()
    })

    ssc.start
    ssc.awaitTermination
    ssc.stop(true, true)
  }


  def createTables() : Unit = {
    implicit val session = new Cluster.Builder()
      .addContactPoint(getEnvironmentOrDefault("CASSANDRA_SERVER", "cassandra"))
      .withPort(9042)
      .build()
      .connect()

    if (session.execute("SELECT count(*) FROM system_schema.keyspaces WHERE keyspace_name = 'sentiment'").one.getLong(0) == 0) {
      session.execute("CREATE KEYSPACE sentiment WITH replication = {'class':'NetworkTopologyStrategy', 'dc1':1, 'dc2':1}")
    }
    if (session.execute("SELECT count(*) FROM system_schema.tables WHERE keyspace_name = 'sentiment' AND table_name = 'tweet_text'").one.getLong(0) == 0) {
      session.execute("CREATE TABLE sentiment.tweet_text (day text, timestamp text, id bigint, keyword text, text text, primary key(day, id))")
    }
    if (session.execute("SELECT count(*) FROM system_schema.tables WHERE keyspace_name = 'sentiment' AND table_name = 'keyword_sentiment'").one.getLong(0) == 0) {
      session.execute("CREATE TABLE sentiment.keyword_sentiment (keyword text, day text, timestamp bigint, sentiment decimal, weighted decimal, count int, primary key((keyword, day), timestamp)) WITH CLUSTERING ORDER BY (timestamp DESC)")
    }
    if (session.execute("SELECT count(*) FROM system_schema.tables WHERE keyspace_name = 'sentiment' AND table_name = 'current_keyword_sentiment'").one.getLong(0) == 0) {
      session.execute("CREATE TABLE sentiment.current_keyword_sentiment (keyword text, sentiment decimal, weighted decimal, count int, primary key(keyword))")
    }

    session.close()
  }
}
