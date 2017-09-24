package model.coin

import model.coin.utils.EmbeddedMariaDb
import model.coin.utils.WorkEntryTestUtil._
import models.coin.WorkEntry
import org.specs2.matcher.Matchers
import play.api.test.{PlaySpecification, WithApplication}

import scala.language.postfixOps

/**
  * Created by julianliebl on 29.01.17.
  */
class WorkEntrySpec extends PlaySpecification with Matchers with EmbeddedMariaDb {
  sequential

  "Database" should {
    "create and find a work entry by id" in new WithApplication() {
      val id: Long = createDummyWorkEntry()

      val workEntry: WorkEntry = WorkEntry.findById(id).get

      workEntry.personId === 1l
      workEntry.area === "dummyArea"
    }
    "create a work entry with the same data" in new WithApplication() {
      val id1First: Long = createDummyWorkEntry()
      val id1Second: Long = createDummyWorkEntry()

      val workEntry1: WorkEntry = WorkEntry.findById(id1First).get
      val workEntry2: WorkEntry = WorkEntry.findById(id1Second).get

      workEntry1.id !=== workEntry2.id
    }
    "create a work entry and delete it" in new WithApplication() {
      val workEntryId: Long = createDummyWorkEntry()

      WorkEntry.findById(workEntryId).isDefined === true

      WorkEntry.remove(workEntryId)

      WorkEntry.findById(workEntryId).isDefined === false
    }
    "create a work entry and edit it" in new WithApplication() {
      val workEntryId: Long = createDummyWorkEntry(area = "beforeEdit")

      val workEntryBefore: WorkEntry = WorkEntry.findById(workEntryId).get

      WorkEntry.edit(workEntryBefore.copy(area = "afterEdit"))

      val workEntryAfter: WorkEntry = WorkEntry.findById(workEntryId).get

      workEntryBefore.copy(area = "afterEdit") === workEntryAfter
    }
  }

}
