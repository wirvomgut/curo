package model.coin.utils

import models.coin.WorkEntry
import org.joda.time.DateTime

/**
  * Created by julianliebl on 01.04.17.
  */
object WorkEntryTestUtil {
  val dummyWorkEntry = WorkEntry(
    id = 1,
    personId = 1,
    kind = "dummyKind",
    area = "dummyArea",
    areaDetail = "dummyAreaDetail",
    description = Some(""),
    timeSpent = 0,
    coins = 0,
    dateDone = DateTime.now
  )

  def createDummyWorkEntry(workEntry: WorkEntry): Unit = {
    createDummyWorkEntry(
      workEntry.personId,
      workEntry.kind,
      workEntry.area,
      workEntry.areaDetail,
      workEntry.description,
      workEntry.timeSpent,
      workEntry.coins,
      workEntry.dateDone
    )
  }

  def createDummyWorkEntry(personId: Long = dummyWorkEntry.personId,
                           kind: String = dummyWorkEntry.kind,
                           area: String = dummyWorkEntry.area,
                           areaDetail: String = dummyWorkEntry.areaDetail,
                           description: Option[String] = dummyWorkEntry.description,
                           timeSpent: Long = dummyWorkEntry.timeSpent,
                           coins: Double = dummyWorkEntry.coins,
                           dateDone: DateTime = dummyWorkEntry.dateDone): Long = WorkEntry.create(
    personId,
    kind,
    area,
    areaDetail,
    description,
    timeSpent,
    coins,
    dateDone = DateTime.now
  )
}
