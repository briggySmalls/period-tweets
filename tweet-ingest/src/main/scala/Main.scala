import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import com.danielasfregola.twitter4s.entities.enums.Language

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {
  implicit val system = ActorSystem("tweet-ingest")

  val source = new TwitterService().source(
    tracks = Seq("period", "menstruation", "menstruating"),
    languages = Seq(Language.English)
  )

  source
    .log("debug", t => println(t.text))
    .to(Sink.ignore)
    .run()
}
