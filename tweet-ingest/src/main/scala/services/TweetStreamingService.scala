package services

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.stream.{Materializer, OverflowStrategy, QueueOfferResult}
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.entities.enums.Language.Language
import com.sksamuel.exts.Logging
import config.TweetStreamingServiceConfig

import scala.concurrent.ExecutionContext
import scala.util.chaining.scalaUtilChainingOps

class TweetStreamingService(settings: TweetStreamingServiceConfig) extends Logging {
  private val client = TwitterStreamingClient()
  private val bufferSize = 100

  def source()
            (implicit materializer: Materializer, ec: ExecutionContext): Source[Tweet, NotUsed] = {

    // Create a source that we can enqueue to dynamically
    val (queue, source) = Source
      .queue[Tweet](bufferSize, OverflowStrategy.dropHead)
      .preMaterialize()

    // Enqueue tweets into the source via the queue
    client.filterStatuses(
        tracks = settings.tracks,
        languages = settings.languages,
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
      .filter(matchesExactly)
  }

  private def matchesExactly(tweet: Tweet): Boolean = {
    settings.exactMatches
      .exists(tweet.text.contains)
      .tap(b => if (!b) logger.debug(s"Tweet ${tweet.id_str} filtered:\n${tweet.text}"))
  }
}
