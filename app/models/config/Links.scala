package models.config

import play.api.{ Configuration, Environment }

/**
 * Created by julianliebl on 13.03.17.
 */
case class Link(url: String, path: Option[String] = None) {
  val target: String = if (path.isDefined) "_self" else "_blank"
}

object Links {
  private val config: Configuration = Configuration.load(Environment.simple())

  val forum: Link = Link(config.get[String]("app.discourse.url"), config.getOptional[String]("app.discourse.path"))
}
