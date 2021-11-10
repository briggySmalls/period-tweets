import akka.NotUsed
import akka.stream.{Materializer, OverflowStrategy, QueueOfferResult}
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueue}
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.entities.enums.Language.Language
import com.sun.org.slf4j.internal.LoggerFactory

import scala.concurrent.ExecutionContext

class TwitterService {
  private val client = TwitterStreamingClient()
  private val bufferSize = 100
  private val logger = LoggerFactory.getLogger(classOf[TwitterService])

  def source(tracks: Seq[String], languages: Seq[Language])
            (implicit materializer: Materializer, ec: ExecutionContext): Source[Tweet, NotUsed] = {
    // Create a source that we can enqueue to dynamically
    val (queue, source) = Source
      .queue[Tweet](bufferSize, OverflowStrategy.dropHead)
      .preMaterialize()

    // Enqueue tweets into the source via the queue
    client.filterStatuses(
        tracks = tracks,
        languages = languages,
        stall_warnings = true,
      )({
        case tweet: Tweet => queue.offer(tweet).map {
          case QueueOfferResult.Enqueued    => logger.debug(s"enqueued ${tweet.id}")
          case QueueOfferResult.Dropped     => logger.warn(s"dropped ${tweet.id}")
          case QueueOfferResult.Failure(ex) => logger.error(s"Offer failed ${ex.getMessage}")
          case QueueOfferResult.QueueClosed => logger.warn("Source Queue closed")
        }
      })

    source
  }
}
