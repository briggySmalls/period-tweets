package clients

import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.script.Script
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties, RequestFailure, RequestSuccess, Response}
import com.sksamuel.exts.Logging
import com.sun.org.slf4j.internal.LoggerFactory
import models.IndexibleTweet

import scala.concurrent.{ExecutionContext, Future}

class ElasticClient(val hosts: Set[String])(implicit ec: ExecutionContext) extends Logging {
  import com.sksamuel.elastic4s.ElasticDsl._

  private val nodes = ElasticProperties(hosts.mkString(","))
  private val client = ElasticClient(JavaClient(nodes))

  def createTweetIndex(name: String): Future[Either[String, Any]] = {
    client.execute {
      createIndex(name)
    }.map(handleResponse)
  }

  def indexTweet(index: String, tweet: IndexibleTweet): Future[Either[String, Any]] = {
    client.execute {
      indexInto(index).id(tweet.id).fields(tweet.toMap() - "id")
    }.map(handleResponse)
  }

  def incrementRetweets(index: String, id: String, count: Long): Future[Either[String, Any]] = {
    client.execute {
      updateById(index, id).script(
        Script(
          "ctx._source.retweet_count += params.count"
        ).params(Map("count" -> count))
      )
    }.map(handleResponse)
  }

  def handleResponse(response: Response[_]): Either[String, Any] =
    response match {
      case failure: RequestFailure => {
        val msg = s"Failed: ${failure.error}"
        logger.error(s"Failed: ${failure.error}")
        Left(msg)
      }
      case results: RequestSuccess[_] => {
        val msg = results.result.toString
        logger.debug(msg)
        Right(results.result)
      }
    }
}
