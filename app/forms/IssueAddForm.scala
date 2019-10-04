package forms

import play.api.data.Form
import play.api.data.Forms._

object IssueAddForm {

  /**
   * A play framework form.
   */
  val form = Form(
    mapping(
      "area" -> nonEmptyText,
      "kind" -> nonEmptyText,
      "title" -> text(minLength = 3, maxLength = 144),
      "description" -> text(minLength = 3, maxLength = 10000),
      "alarm" -> boolean
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   *
   * @param area Area of issue.
   * @param kind Kind of issue.
   * @param title Title of the issue.
   * @param description Description of the issue.
   */
  case class Data(
    area: String,
    kind: String,
    title: String,
    description: String,
    alarm: Boolean
  )
}
