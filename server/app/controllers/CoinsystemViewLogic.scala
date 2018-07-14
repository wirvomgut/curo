package controllers

import models.User
import models.coin.{ Person, WorkEntry }
import models.common.Pagination

object CoinsystemViewLogic {
  def apply(user: User, pagination: Pagination = Pagination()): CoinsystemViewLogic = new CoinsystemViewLogic(user, pagination)
}

class CoinsystemViewLogic(user: User, pagination: Pagination) {
  val userId: String = user.loginInfo.providerKey

  val person: Person = Person.findOrCreateByUid(userId)
  val workEntries: Seq[WorkEntry] = Person.findWorkEntries(person.id, offset = pagination.offset)
  val workEntryCount: Long = Person.countWorkEntries(person.id)
  val pagesCount: Int = pagination.pages(workEntryCount)

  val showPagination: Boolean = pagination.pages(workEntryCount) > 1
  val showBackButton: Boolean = pagination.offset > 0
  val showNextButton: Boolean = pagination.offset + pagination.limit < workEntryCount

  val prevPageNumber: Int = pagination.currentPage - 1
  val nextPageNumber: Int = pagination.currentPage + 1
}
