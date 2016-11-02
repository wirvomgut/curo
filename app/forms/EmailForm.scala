package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the change of passwords.
 */
object EmailForm {

  val form = Form(
    mapping(
      "email-new" -> email
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   *
   * @param emailNew The new e-mail of the user.
   */
  case class Data(
    emailNew: String
  )
}
