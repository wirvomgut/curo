package models.coin

import models.coin.Person.PersonId
import models.coin.WorkEntry.WorkEntryId
import org.joda.time.DateTime
import play.api.libs.json._
import scalikejdbc._

import scala.concurrent.duration._

case class WorkEntry(id: WorkEntryId, personId: PersonId, kind: String, area: String, areaDetail: String, description: Option[String], timeSpent: Long, coins: Double, dateDone: DateTime, dateCreated: DateTime = DateTime.now) {
  implicit val jsonWrites: Writes[WorkEntry] = WorkEntry.workEntryWrites

  val prettyTimeSpent: String = s"0${timeSpent.minutes.toHours}:".takeRight(3) + s"0${timeSpent.minutes.minus(timeSpent.minutes.toHours.hours).toMinutes}".takeRight(2)

  def toJson: String = {
    Json.toJson(this).toString
  }
}

object WorkEntry extends SQLSyntaxSupport[WorkEntry] {
  override val tableName = "work_entries"

  type WorkEntryId = Long

  val w: scalikejdbc.QuerySQLSyntaxProvider[scalikejdbc.SQLSyntaxSupport[WorkEntry], WorkEntry] = WorkEntry.syntax
  val wc: scalikejdbc.ColumnName[WorkEntry] = WorkEntry.column

  def create(personId: PersonId, kind: String, area: String, areaDetail: String, description: Option[String], timeSpent: Long, coins: Double, dateDone: DateTime)(implicit s: DBSession = AutoSession): Long = {
    withSQL {
      insert.into(WorkEntry).namedValues(
        wc.personId -> personId,
        wc.kind -> kind,
        wc.area -> area,
        wc.areaDetail -> areaDetail,
        wc.description -> description,
        wc.timeSpent -> timeSpent,
        wc.coins -> coins,
        wc.dateDone -> dateDone
      )
    }.updateAndReturnGeneratedKey().apply()
  }

  def edit(workEntry: WorkEntry)(implicit s: DBSession = AutoSession): Int = {
    withSQL {
      update(WorkEntry).set(
        wc.kind -> workEntry.kind,
        wc.area -> workEntry.area,
        wc.areaDetail -> workEntry.areaDetail,
        wc.description -> workEntry.description,
        wc.timeSpent -> workEntry.timeSpent,
        wc.coins -> workEntry.coins,
        wc.dateDone -> workEntry.dateDone
      ).where.eq(wc.id, workEntry.id)
    }.update.apply()
  }

  def findById(id: WorkEntryId)(implicit s: DBSession = AutoSession): Option[WorkEntry] = {
    withSQL {
      select.from(WorkEntry as w).where.eq(wc.id, id)
    }.map(WorkEntry(w)).single.apply()
  }

  def remove(id: WorkEntryId)(implicit s: DBSession = AutoSession): Int = {
    withSQL {
      delete.from(WorkEntry).where.eq(wc.id, id)
    }.update().apply()
  }

  def apply(w: SyntaxProvider[WorkEntry])(rs: WrappedResultSet): WorkEntry = {
    apply(w.resultName)(rs)
  }

  def apply(r: ResultName[WorkEntry])(rs: WrappedResultSet): WorkEntry = new WorkEntry(
    id = rs.long(r.id),
    personId = rs.long(r.personId),
    kind = rs.string(r.kind),
    area = rs.string(r.area),
    areaDetail = rs.string(r.areaDetail),
    description = rs.stringOpt(r.description),
    timeSpent = rs.long(r.timeSpent), coins = rs.double(r.coins),
    dateDone = rs.jodaDateTime(r.dateDone),
    dateCreated = rs.jodaDateTime(r.dateCreated)
  )

  implicit val workEntryWrites: Writes[WorkEntry] = new Writes[WorkEntry] {
    def writes(workEntry: WorkEntry): JsObject = Json.obj(
      "id" -> workEntry.id,
      "personId" -> workEntry.personId,
      "kind" -> workEntry.kind,
      "area" -> workEntry.area,
      "areaDetail" -> workEntry.areaDetail,
      "description" -> workEntry.description,
      "timeSpent" -> workEntry.timeSpent,
      "coins" -> workEntry.coins,
      "dateDone" -> workEntry.dateDone,
      "dateCreated" -> workEntry.dateCreated
    )
  }
}