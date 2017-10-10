package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the change of passwords.
 */
object NoPasswordForm {

  val form = Form(
    mapping(
      "uid" -> nonEmptyText,
      "password-new" -> nonEmptyText,
      "password-confirm" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   *
   * @param passwordNew The new password of the user.
   * @param passwordConfirm The new confirmed password of the user.
   */
  case class Data(
    uid: String,
    passwordNew: String,
    passwordConfirm: String)
}
