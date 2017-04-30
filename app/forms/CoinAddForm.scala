package forms

import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._

import scala.collection.immutable

/**
  * The form which handles the submission of the credentials.
  */
object CoinAddForm {

  val timeValues = Seq(
    "15" -> "00:15",
    "30" -> "00:30",
    "45" -> "00:45",
    "60" -> "01:00",
    "90" -> "01:30",
    "120" -> "02:00",
    "150" -> "02:30",
    "180" -> "03:00",
    "210" -> "03:30",
    "240" -> "04:00",
    "300" -> "05:00",
    "360" -> "06:00",
    "420" -> "07:00",
    "480" -> "08:00",
    "540" -> "09:00",
    "600" -> "10:00",
    "660" -> "11:00",
    "720" -> "12:00"
  )

  val coinValues: Seq[(String, String)] = (0 until 12).map(v => v.toString -> v.toString)

  /**
    * A play framework form.
    */
  val form = Form(
    mapping(
      "area" -> nonEmptyText,
      "description" -> nonEmptyText(minLength=3, maxLength=255),
      "time" -> longNumber(min=0, max=720),
      "coin" -> number(min=0, max=12),
      "date" -> jodaDate
    )(Data.apply)(Data.unapply)
  )

  /**
    * The form data.
    *
    * @param area Area where the task was executed.
    * @param description Description of the task.
    * @param time Time spent executing this task.
    * @param coin Coin value issued for this task.
    * @param date Date when the task was done.
    */
  case class Data(
                   area: String,
                   description: String,
                   time: Long,
                   coin: Int,
                   date: DateTime
                 )
}
