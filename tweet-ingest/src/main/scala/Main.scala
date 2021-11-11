import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import clients.ElasticClient
import com.danielasfregola.twitter4s.entities.enums.Language
import com.sksamuel.exts.Logging
import com.typesafe.config.ConfigFactory
import config.TweetStreamingServiceConfig
import models.IndexibleTweet
import services.{TweetIndexerService, TweetStreamingService}

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App with Logging {
  implicit val system = ActorSystem("tweet-ingest")

  logger.info("Starting!")

  val config = ConfigFactory.load("application.conf")
  val twitterServiceConfig = TweetStreamingServiceConfig.fromConfig(config.getConfig("twitter.service"))
  val streamingService = new TweetStreamingService(twitterServiceConfig)

  val es = new ElasticClient(Set("http://localhost:9200"))
  val esService = new TweetIndexerService(es)

  esService.init().foreach(_ =>
   streamingService.source
    .log("debug", t => println(t.text))
    .to(Sink.foreach(esService.handleTweet))
    .run()
  )
}
