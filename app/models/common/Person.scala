package models.common

import models.coin.WorkEntry
import models.common.Person.{PersonId, PersonUid}
import scalikejdbc._
import sqls.count

case class Person(id: PersonId, uid: PersonUid)

object Person extends SQLSyntaxSupport[Person] {
  override val tableName = "persons"

  type PersonId = Long
  type PersonUid = String

  val p: scalikejdbc.QuerySQLSyntaxProvider[scalikejdbc.SQLSyntaxSupport[Person], Person] = Person.syntax("p")
  val w: scalikejdbc.QuerySQLSyntaxProvider[scalikejdbc.SQLSyntaxSupport[WorkEntry], WorkEntry] = WorkEntry.syntax("w")
  val pc: scalikejdbc.ColumnName[Person] = Person.column

  def create(uid: String)(implicit s: DBSession = AutoSession): PersonId = {
    withSQL {
      insert.into(Person).namedValues(pc.uid -> uid)
    }.updateAndReturnGeneratedKey().apply()
  }

  def findById(id: PersonId)(implicit s: DBSession = AutoSession): Option[Person] = {
    withSQL {
      select.from(Person as p).where.eq(pc.id, id)
    }.map(Person(p)).single.apply()
  }

  def findByUid(uid: PersonUid)(implicit s: DBSession = AutoSession): Option[Person] = {
    withSQL {
      select.from(Person as p).where.eq(pc.uid, uid)
    }.map(Person(p)).single.apply()
  }

  def findOrCreateByUid(uid: PersonUid)(implicit s: DBSession = AutoSession): Person = {
    findByUid(uid).getOrElse(findById(create(uid)).get)
  }

  def findWorkEntries(id: PersonId, limit: Int = 9, offset: Int = 0)(implicit s: DBSession = AutoSession): Seq[WorkEntry] = {
    withSQL {
      select.from(WorkEntry as w).where.eq(w.personId, id).orderBy(w.dateDone).desc.limit(limit).offset(offset)
    }.map(WorkEntry(w)).collection.apply()
  }

  def countWorkEntries(id: PersonId)(implicit s: DBSession = AutoSession): Long = {
    withSQL {
      select(count).from(WorkEntry as w).where.eq(w.personId, id)
    }.map(_.long(1)).single.apply().get
  }

  def apply(p: SyntaxProvider[Person])(rs: WrappedResultSet): Person = {
    apply(p.resultName)(rs)
  }

  def apply(p: ResultName[Person])(rs: WrappedResultSet): Person = {
    new Person(rs.long(p.id), rs.string(p.uid))
  }
}
