import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.entities.enums.Language
import com.danielasfregola.twitter4s.entities.streaming.StreamingMessage

object Main extends App {
  val client = TwitterStreamingClient()

  def printTweetText: PartialFunction[StreamingMessage, Unit] = {
    case tweet: Tweet => println(tweet.text)
  }

  val streamF = client.filterStatuses(
    tracks = Seq("period pants", "period knickers", "period underwear", "sanitary shorts", "period panties", "menstruation"),
    languages = Seq(Language.English),
    stall_warnings = true,
  )({
    case tweet: Tweet => println(tweet.text)
  },
  {
    case e: Throwable => println(e)
  })
//  client.sampleStatuses(stall_warnings = true)(printTweetText)
}
