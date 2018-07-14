package forms

import models.coin.{ Person, WorkEntry }
import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.JodaForms._
import play.api.data.format.Formats
import play.api.libs.json._

import scala.concurrent.duration._

/**
 * The form which handles the submission of the credentials.
 */
object CoinAddForm {

  val kindValues: Seq[(String, String)] = Seq(
    "Teilnahme an Sitzungen",
    "Kopfarbeit (z.B. Arbeit am PC, Organisation)",
    "Praktische Arbeit (wiederkehrend)",
    "Praktische Arbeit (einmalig)").map(v => v -> v)

  def valueName(area: String, subArea: String): String = s"${area.toLowerCase}_${subArea.toLowerCase}"
    .replaceAll("ü", "ue")
    .replaceAll("ö", "oe")
    .replaceAll("ä", "ae")
    .replaceAll("ß", "ss")
    .replaceAll(" ", "_")

  case class TaskArea(areaName: String, subAreas: Seq[String]) {
    def valueName(subArea: String): String = CoinAddForm.valueName(areaName, subArea)
  }

  case class TaskAreas(areas: Seq[TaskArea])
  implicit val taskAreaWrites: Writes[TaskArea] = Json.writes[TaskArea]
  implicit val taskAreasWrites: Writes[TaskAreas] = Json.writes[TaskAreas]

  private val taskAreaValues: TaskAreas = TaskAreas(Seq(
    TaskArea("AG Gutsarbeiten", Seq(
      "AG Gutsarbeiten",
      "AK Coinsystem",
      "AK Einkauf",
      "AK Garten und Aussengelände",
      "AK Kochen",
      "AK Naturschutz",
      "AK Putzen",
      "AK Raumpaten")),
    TaskArea("AG Gemeinschaft", Seq(
      "AG Gemeinschaft",
      "AK Gemeinschaftsaktionen",
      "AK Gemeinschaftsbildung",
      "AK Interessenten",
      "AK Kommunikationsformen",
      "AK NUSS-Knacker",
      "AK Vertrauensrat",
      "AK ZusammenLeben")),
    TaskArea("AG Markt und Kommunikation", Seq(
      "AG Markt und Kommunikation",
      "AK Coworking",
      "AK interne Kommunikation",
      "AK Öffentlichkeitsarbeit",
      "AK Übernachtung",
      "AK Veranstaltungen")),
    TaskArea("AG Raute", Seq(
      "AG Raute",
      "AK Aussenbereichsplanung",
      "AK Haustechnik",
      "AK Innenraumplanung",
      "AK Digitalisierung",
      "AK Mobilität",
      "AK Reaktivierung",
      "Umweltbeauftragte")),
    TaskArea("Von Gutsleuten für Gutsleute", Seq(
      "Café",
      "Chor",
      "Digitales Gut",
      "Yoga",
      "kleiner Filmclub",
      "Sonstiges")),
    TaskArea("Verwaltung der Gemeinschaft", Seq(
      "Runder Tisch",
      "Beiräte",
      "Plenum")),
    TaskArea("Verwaltung der Genossenschaft", Seq(
      "Vorstand",
      "Aufsichtsrat",
      "Stabsstellen",
      "Generalversammlung",
      "Sonstiges")),
    TaskArea("Sonstiges", Seq(""))))

  case class AreaAndDetail(area: String, detail: String)
  val areaValueNameToAreaDetail: Map[String, AreaAndDetail] = taskAreaValues
    .areas
    .flatMap(a => a.subAreas.map(s => a.valueName(s) -> AreaAndDetail(a.areaName, s)))
    .toMap

  //TODO: make this nice ;)
  val areaValues: Seq[(String, String)] = taskAreaValues
    .areas
    .flatMap(a => a.subAreas.map(s => a.valueName(s) -> (if (s.isEmpty || s == a.areaName) s"${a.areaName}" else s"${a.areaName} / " + s)))

  def subAreasForArea(areaName: String): Seq[String] = taskAreaValues.areas.find(_.areaName == areaName).map(_.subAreas).getOrElse(Seq.empty)

  val timeValues: Seq[(String, String)] =
    ((30 until 300 by 30) ++ (300 until 780 by 60))
      .map(ints => ints.toString -> ints.minutes)
      .map(m => m._1 -> (s"0${m._2.toHours}:".takeRight(3) + s"0${m._2.minus(m._2.toHours.hours).toMinutes}".takeRight(2)))

  private val coinRawValues: Seq[Double] = (0 until 29).map(v => v * 0.5)
  val coinValues: Seq[(String, String)] =
    coinRawValues.map(v => v.toString -> v.toString.stripSuffix(".0"))

  def fill(workEntry: WorkEntry): Data = {
    Data(
      id = Some(workEntry.id),
      personId = Some(workEntry.personId),
      kind = workEntry.kind,
      area = valueName(workEntry.area, workEntry.areaDetail),
      description = workEntry.description,
      time = workEntry.timeSpent,
      coin = workEntry.coins,
      date = workEntry.dateDone)
  }

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "id" -> optional(longNumber),
      "personId" -> optional(longNumber),
      "kind" -> nonEmptyText,
      "area" -> nonEmptyText,
      "description" -> optional(text(minLength = 3, maxLength = 255)),
      "time" -> longNumber(min = 0, max = 720),
      "coin" -> of[Double](Formats.doubleFormat).verifying(d => coinRawValues.contains(d)),
      "date" -> jodaDate("dd.MM.yyyy"))(Data.apply)(Data.unapply))

  /**
   * The form data.
   *
   * @param id optional work entry id
   * @param personId optional person id
   * @param kind Kind of task.
   * @param area Area where the task was executed.
   * @param description Description of the task.
   * @param time Time spent executing this task.
   * @param coin Coin value issued for this task.
   * @param date Date when the task was done.
   */
  case class Data(
    id: Option[WorkEntry.WorkEntryId] = None,
    personId: Option[Person.PersonId] = None,
    kind: String,
    area: String,
    description: Option[String],
    time: Long,
    coin: Double,
    date: DateTime)
}
