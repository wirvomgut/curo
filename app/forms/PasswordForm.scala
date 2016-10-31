package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the change of passwords.
 */
object PasswordForm {

  val form = Form(
    mapping(
      "password-old" -> nonEmptyText,
      "password-new" -> nonEmptyText,
      "password-confirm" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   *
   * @param passwordOld The old password of the user.
   * @param passwordNew The new password of the user.
   * @param passwordConfirm The new confirmed password of the user.
   */
  case class Data(
    passwordOld: String,
    passwordNew: String,
    passwordConfirm: String)
}
