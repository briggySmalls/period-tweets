package models

import com.danielasfregola.twitter4s.entities.Tweet

import java.time.Instant

case class IndexibleTweet(
  val id: Long,
  val text: String,
  val created: Instant,
  val retweet_count: Long,
) {
  def toMap() = Map(
    "id" -> id,
    "text" -> text,
    "created" -> created,
    "retweet_count" -> retweet_count,
  )
}

object IndexibleTweet {
  def fromTweet(t: Tweet) =
    IndexibleTweet(
      id = t.id,
      text = t.text,
      created = t.created_at,
      retweet_count = t.retweet_count,
    )
}