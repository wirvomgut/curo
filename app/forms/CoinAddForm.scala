package forms

import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._

import scala.concurrent.duration._

/**
  * The form which handles the submission of the credentials.
  */
object CoinAddForm {

  val areaValues: Seq[(String, String)] = Seq(
    "Herrenhaus",
    "Nordflügel",
    "Remise",
    "Scheune",
    "Kleine Reithalle",
    "Große Reithalle",
    "Innenhöfe",
    "Aussengelände"
  ).map(v => v -> v)

  val taskValues: Seq[(String, String)] = Seq(
    "Verwaltung des Gutes",
    "Instandhaltung und Technik",
    "Raumpflege",
    "Garten und Schneeräumen",
    "Gemeinschaft",
    "Kommunikation- und Öffentlichkeitsarbeit",
    "Servicepoint und Anlaufstelle",
    "Hauswirtschaft / Kochen"
  ).map(v => v -> v)

  val timeValues: Seq[(String, String)] =
    (Seq(15, 30, 45) ++ (60 until 300 by 30) ++ (300 until 780 by 60))
      .map(ints => ints.toString -> ints.minutes)
      .map(m => m._1 -> (s"0${m._2.toHours}:".takeRight(3) + s"0${m._2.minus(m._2.toHours.hours).toMinutes}".takeRight(2)))

  val coinValues: Seq[(String, String)] =
    (0 until 13).map(v => v.toString -> v.toString)

  /**
    * A play framework form.
    */
  val form = Form(
    mapping(
      "area" -> nonEmptyText,
      "task" -> nonEmptyText,
      "description" -> nonEmptyText(minLength=3, maxLength=255),
      "time" -> longNumber(min=0, max=720),
      "coin" -> number(min=0, max=12),
      "date" -> jodaDate("d.m.y")
    )(Data.apply)(Data.unapply)
  )

  /**
    * The form data.
    *
    * @param area Area where the task was executed.
    * @param task Task area where the task was executed.
    * @param description Description of the task.
    * @param time Time spent executing this task.
    * @param coin Coin value issued for this task.
    * @param date Date when the task was done.
    */
  case class Data(
                   area: String,
                   task: String,
                   description: String,
                   time: Long,
                   coin: Int,
                   date: DateTime
                 )
}
