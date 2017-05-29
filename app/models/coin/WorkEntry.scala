package models.coin

import models.coin.Person.PersonId
import models.coin.WorkEntry.WorkEntryId
import org.joda.time.DateTime
import scalikejdbc._

import scala.concurrent.duration._

case class WorkEntry(id: WorkEntryId, personId: PersonId, area: String, task: String, description: String, timeSpent: Long, coins: Double, dateDone: DateTime, dateCreated: DateTime = DateTime.now) {
  val prettyTimeSpent: String = s"0${timeSpent.minutes.toHours}:".takeRight(3) + s"0${timeSpent.minutes.minus(timeSpent.minutes.toHours.hours).toMinutes}".takeRight(2)
}

object WorkEntry extends SQLSyntaxSupport[WorkEntry] {
  override val tableName = "work_entries"

  type WorkEntryId = Long

  val w: scalikejdbc.QuerySQLSyntaxProvider[scalikejdbc.SQLSyntaxSupport[WorkEntry], WorkEntry] = WorkEntry.syntax
  val wc: scalikejdbc.ColumnName[WorkEntry] = WorkEntry.column

  def create(personId: PersonId, area: String, task: String, description: String, timeSpent: Long, coins: Double, dateDone: DateTime)(implicit s: DBSession = AutoSession): Long = {
    withSQL {
      insert.into(WorkEntry).namedValues(
        wc.personId -> personId,
        wc.area -> area,
        wc.task -> task,
        wc.description -> description,
        wc.timeSpent -> timeSpent,
        wc.coins -> coins,
        wc.dateDone -> dateDone
      )
    }.updateAndReturnGeneratedKey().apply()
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

  def apply(r: ResultName[WorkEntry])(rs: WrappedResultSet): WorkEntry = {
    new WorkEntry(rs.long(r.id), rs.long(r.personId), rs.string(r.area), rs.string(r.task), rs.string(r.description), rs.long(r.timeSpent), rs.double(r.coins), rs.jodaDateTime(r.dateDone), rs.jodaDateTime(r.dateCreated))
  }
}