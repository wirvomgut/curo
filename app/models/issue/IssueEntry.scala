package models.issue

import java.time.{Instant, ZoneId, ZoneOffset}

import akka.http.scaladsl.model.headers.LinkParams.title
import services.KanboardResponse.KanboardTask

case class IssueEntry(
                       dateReported: Instant,
                       dateModified: Instant,
                       dateCompleted: Option[Instant],
                       title: String,
                       description: Option[String],
                       assignee: Option[String],
                       area: Option[String],
                       kind: Option[String],
                       alarm: Option[Boolean] = Some(false)
                     ) {

  val prettyDateReported: String = dateReported.atZone(ZoneId.of("GMT+1")).toLocalDate.format(IssueEntry.dateFormatter)

  val color: String = "" match {
    case _ if dateCompleted.isDefined => "green"
    case _ if dateCompleted.isEmpty && assignee.isEmpty => "red"
    case _ if dateCompleted.isEmpty => "orange"
    case _ => "grey"
  }

  val status: String = "" match {
    case _ if dateCompleted.isDefined => "issue.status.done"
    case _ if dateCompleted.isEmpty && assignee.isEmpty => "issue.status.new"
    case _ if dateCompleted.isEmpty => "issue.status.progress"
    case _ => "issue.status.unknown"
  }

  val shortDescription: Option[String] = description.map(shortenString(_))

  private def shortenString(s:String, at: Int = 46): String = {
    if(s.length > at)
      s.substring(0, at) + "..."
    else s
  }
}

object IssueEntry {

  import java.time.format.DateTimeFormatter

  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

  def fromKanboardTask(kanboardTask: KanboardTask) = IssueEntry(
    dateReported = Instant.ofEpochSecond(kanboardTask.date_creation.get.toLong),
    dateModified = Instant.ofEpochSecond(kanboardTask.date_creation.get.toLong),
    dateCompleted = kanboardTask.date_completed.map(_.toLong).map(Instant.ofEpochSecond),
    title = kanboardTask.title.get,
    description = kanboardTask.description,
    assignee = kanboardTask.assignee_name,
    area = kanboardTask.category_name,
    kind = kanboardTask.swimlane_name
  )
}