package views.logic

import java.time.Instant

import models.User
import models.common.{Pagination, Person}
import models.issue.IssueEntry
import services.KanboardService

import scala.concurrent.{ExecutionContext, Future}

case class IssueSystemViewLogic(
  userId: String,
  person: Person,
  issueEntries: Seq[IssueEntry],
  issueEntryCount: Long,
  pagesCount: Int,
  showPagination: Boolean,
  showBackButton: Boolean,
  showNextButton: Boolean,
  prevPageNumber: Int,
  nextPageNumber: Int,
  area: Seq[(String, String)],
  kind: Seq[(String, String)]
) {

}


object IssueSystemViewLogic {
  def create(user: User, kanboardService: KanboardService, pagination: Pagination = Pagination())(implicit ec: ExecutionContext): Future[IssueSystemViewLogic] = {
    val userId = user.loginInfo.providerKey
    val person = Person.findOrCreateByUid(userId)

    for {
      tasks <- kanboardService.tasksReportedBy(person.uid)
    } yield {
      val issueEntries: Seq[IssueEntry] = tasks.result.map(IssueEntry.fromKanboardTask).sortBy(_.dateReported)(Ordering[Instant].reverse)
      val issueEntryCount: Long = issueEntries.size.toLong

      IssueSystemViewLogic(
        userId = userId,
        person = Person.findOrCreateByUid(userId),
        issueEntries = issueEntries,
        issueEntryCount = issueEntryCount,
        pagesCount = pagination.pages(issueEntryCount),

        showPagination = pagination.pages(issueEntryCount) > 1,
        showBackButton = pagination.offset > 0,
        showNextButton = pagination.offset + pagination.limit < issueEntryCount,

        prevPageNumber = pagination.currentPage - 1,
        nextPageNumber = pagination.currentPage + 1,

        area = kanboardService.categories.map(v => v.id -> v.name),
        kind = kanboardService.swimlanes.map(v => v.id -> v.name),
      )
    }
  }
}