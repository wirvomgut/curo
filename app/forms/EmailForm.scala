package forms

import play.api.data.Form
import play.api.data.Forms._

/**
 * The form which handles the change of the e-mail.
 */
object EmailForm {

  val form: Form[Data] = Form(
    mapping(
      "email-new" -> email)(Data.apply)(Data.unapply))

  /**
   * The form data.
   *
   * @param emailNew The new e-mail of the user.
   */
  case class Data(
    emailNew: String)

  object Data {
    def unapply(d: Data): Option[String] = Some((d.emailNew))
  }
}
