package services

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.ws.{EmptyBody, WSClient, WSResponse}
import services.PushoverPriority.PushoverPriority

import scala.concurrent.Future

object PushoverPriority extends Enumeration {
  type PushoverPriority = Value
  val NO_NOTIFICATION, QUIET_NOTIFICATION, LOUD_NOTIFICATION, LOUD_CONFIRM_NOTIFICATION = Value

  def toInt(pushoverPriority: PushoverPriority): Int = pushoverPriority match {
    case NO_NOTIFICATION => -2
    case QUIET_NOTIFICATION => -1
    case LOUD_NOTIFICATION => 1
    case LOUD_CONFIRM_NOTIFICATION => 2
    case _ => 0
  }
}

@Singleton
class PushoverService @Inject()(config: Configuration, ws: WSClient) {
  val token: String = config.get[String]("pushover.token")
  val groupKey: String = config.get[String]("pushover.group.key")

  def sendMessage(title: String, message: String, priority: PushoverPriority, url: String): Future[WSResponse] = {
    val additionalParams: Map[String, String] = priority match {
      case PushoverPriority.LOUD_CONFIRM_NOTIFICATION => Map(
        "expire" -> "10800",
        "retry" -> "600"
      )
      case _ => Map()
    }

    ws.url("https://api.pushover.net/1/messages.json")
      .post(
        Map(
          "token" -> token,
          "user" -> groupKey,
          "title" -> title,
          "message" -> message,
          "priority" -> PushoverPriority.toInt(priority).toString,
          "url" -> url
        ) ++ additionalParams
      )
  }
}
