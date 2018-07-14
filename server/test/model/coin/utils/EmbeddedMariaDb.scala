package model.coin.utils

import ch.vorburger.mariadb4j.DB
import org.specs2.specification.BeforeAfterEach

/**
  * Created by privat on 21.05.17.
  */
trait EmbeddedMariaDb extends BeforeAfterEach {

  var db: DB = _
  override protected def before: Any = {
    db = DB.newEmbeddedDB(3306)
    db.start()
  }

  override protected def after: Any = {
    db.stop()
  }
}
