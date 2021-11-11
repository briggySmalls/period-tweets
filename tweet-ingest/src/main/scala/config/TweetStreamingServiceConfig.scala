package config

import com.danielasfregola.twitter4s.entities.enums.Language
import com.danielasfregola.twitter4s.entities.enums.Language.Language
import com.typesafe.config.Config

import scala.jdk.CollectionConverters._

case class TweetStreamingServiceConfig (
                                tracks: Seq[String],
                                languages: Seq[Language],
                                exactMatches: Seq[String],
                                )

object TweetStreamingServiceConfig {
  def fromConfig(config: Config): TweetStreamingServiceConfig =
    TweetStreamingServiceConfig(
      tracks = config.getStringList("tracks").asScala.toList,
      languages = config.getStringList("languages").asScala.toList.map(
        Language.withName
      ),
      exactMatches = config.getStringList("exactMatches").asScala.toList,
    )
}
