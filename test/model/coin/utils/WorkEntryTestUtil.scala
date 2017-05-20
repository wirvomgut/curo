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
    area = "dummyArea",
    task = "dummyTask",
    description = "",
    timeSpent = 0,
    coins = 0,
    dateDone = DateTime.now
  )

  def createDummyWorkEntry(workEntry: WorkEntry): Unit ={
    createDummyWorkEntry(
      workEntry.personId,
      workEntry.area,
      workEntry.task,
      workEntry.description,
      workEntry.timeSpent,
      workEntry.coins,
      workEntry.dateDone
    )
  }

  def createDummyWorkEntry(personId: Long = dummyWorkEntry.personId,
                           area: String = dummyWorkEntry.area,
                           task: String = dummyWorkEntry.task,
                           description: String = dummyWorkEntry.description,
                           timeSpent: Long = dummyWorkEntry.timeSpent,
                           coins: Int = dummyWorkEntry.coins,
                           dateDone: DateTime = dummyWorkEntry.dateDone): Long = WorkEntry.create(
    personId,
    area,
    task,
    description,
    timeSpent,
    coins,
    dateDone = DateTime.now
  )
}
