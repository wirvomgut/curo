package models.config

import play.api.{ Configuration, Play }

/**
 * Created by julianliebl on 13.03.17.
 */
case class Link(url: String, path: Option[String] = None) {
  val target: String = if (path.isDefined) "_self" else "_blank"
}

object Links {
  private val config: Configuration = Play.configuration(Play.current)

  val forum: Link = Link(config.getString("app.discourse.url").get, config.getString("app.discourse.path"))
}
