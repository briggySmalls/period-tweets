import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import clients.ElasticClient
import com.danielasfregola.twitter4s.entities.enums.Language
import models.IndexibleTweet

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {
  implicit val system = ActorSystem("tweet-ingest")

  val source = new TwitterService().source(
    tracks = Seq("period", "menstruation", "menstruating"),
    languages = Seq(Language.English)
  )



  val indexName = "tweets"
  val es = new ElasticClient(Set("http://localhost:9200"))
  es.createTweetIndex(indexName).map(o => {
    source
    .log("debug", t => println(t.text))
    .to(Sink.foreach(t => es.indexTweet(indexName, IndexibleTweet.fromTweet(t))))
    .run()
  })
}
