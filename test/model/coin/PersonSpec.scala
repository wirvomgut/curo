package model.coin

import model.coin.utils.EmbeddedMariaDb
import model.coin.utils.WorkEntryTestUtil._
import models.coin.WorkEntry
import models.common.Person
import models.common.Person.PersonId
import org.specs2.matcher.Matchers
import play.api.test.{PlaySpecification, WithApplication}

import scala.language.postfixOps

/**
  * Created by julianliebl on 29.01.17.
  */
class PersonSpec extends PlaySpecification with Matchers with EmbeddedMariaDb {
  sequential

  "Database" should {
    "create and find a person by id" in new WithApplication() {
      val id: PersonId = Person.create("testuid")

      val person: Person = Person.findById(id).get

      person.uid must be equalTo "testuid"
    }
    "create and find a person by uid" in new WithApplication() {
      val id: PersonId = Person.create("testuid")

      val person: Person = Person.findByUid("testuid").get

      person.id === id
      person.uid ==="testuid"
    }
    "find and create a not existent person by uid" in new WithApplication() {
      val person1: Person = Person.findOrCreateByUid("testuid")
      val person2: Person = Person.findOrCreateByUid("testuid")

      person1.id === 1
      person1.uid ==="testuid"

      person2.id === 1
      person2.uid ==="testuid"
    }
    "not create a person with the same uid" in new WithApplication() {
      val id1First: PersonId = Person.create("test1")
      Person.create("test1") must throwA[com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException]

      id1First must be equalTo 1
    }
    "add work entries and find them by uid" in new WithApplication() {
      val personId1: PersonId = Person.create("personId1")
      val personId2: PersonId = Person.create("personId2")

      createDummyWorkEntry(dummyWorkEntry.copy(personId = personId1))
      createDummyWorkEntry(dummyWorkEntry.copy(personId = personId1))
      createDummyWorkEntry(dummyWorkEntry.copy(personId = personId2))

      val workEntries1: Seq[WorkEntry] = Person.findWorkEntries(personId1)
      val workEntries2: Seq[WorkEntry] = Person.findWorkEntries(personId2)

      workEntries1.size === 2
      workEntries2.size === 1
    }
    "add work entries and count them" in new WithApplication() {
      val personId1: PersonId = Person.create("personId1")
      val personId2: PersonId = Person.create("personId2")

      createDummyWorkEntry(dummyWorkEntry.copy(personId = personId1))
      createDummyWorkEntry(dummyWorkEntry.copy(personId = personId1))
      createDummyWorkEntry(dummyWorkEntry.copy(personId = personId2))

      val workEntriesCount1: Long = Person.countWorkEntries(personId1)
      val workEntriesCount2: Long = Person.countWorkEntries(personId2)

      workEntriesCount1 == 2
      workEntriesCount2 == 1
    }
  }
}
