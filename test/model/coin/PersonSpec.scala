package model.coin

import models.coin.Person
import org.specs2.matcher.Matchers
import play.api.test.{PlaySpecification, WithApplication}

import scala.language.postfixOps

import model.coin.utils.WorkEntryTestUtil._

/**
  * Created by julianliebl on 29.01.17.
  */
class PersonSpec extends PlaySpecification with Matchers {
  sequential

  "Database" should {
    "create and find a person by id" in new WithApplication() {
      val id = Person.create("testuid")

      val person = Person.findById(id).get

      person.uid must be equalTo "testuid"
    }
    "create and find a person by uid" in new WithApplication() {
      val id = Person.create("testuid")

      val person = Person.findByUid("testuid").get

      person.id === id
      person.uid ==="testuid"
    }
    "find and create a not existent person by uid" in new WithApplication() {
      val person1 = Person.findOrCreateByUid("testuid")
      val person2 = Person.findOrCreateByUid("testuid")

      person1.id === 1
      person1.uid ==="testuid"

      person2.id === 1
      person2.uid ==="testuid"
    }
    "not create a person with the same uid" in new WithApplication() {
      val id1First = Person.create("test1")
      Person.create("test1") must throwA[org.h2.jdbc.JdbcSQLException]

      id1First must be equalTo 1
    }
    "add work entries and find them by uid" in new WithApplication() {
      val personId1 = Person.create("personId1")
      val personId2 = Person.create("personId2")

      val workEntry1 = createDummyWorkEntry(dummyWorkEntry.copy(personId = personId1))
      val workEntry2 = createDummyWorkEntry(dummyWorkEntry.copy(personId = personId1))
      val workEntry3 = createDummyWorkEntry(dummyWorkEntry.copy(personId = personId2))

      val workEntries1 = Person.findWorkEntries(personId1)
      val workEntries2 = Person.findWorkEntries(personId2)

      workEntries1.size === 2
      workEntries2.size === 1
    }

  }

}
