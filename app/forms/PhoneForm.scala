package forms

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

/**
 * The form which handles the change of the phone number.
 */
object PhoneForm {

  val phoneCheckConstraint: Constraint[String] = Constraint("constraints.phonecheck")({
    plainText =>
      val isPhoneNumber = plainText.matches("""^(?:([+][0-9]{1,2})+[ .-]*)?([(]{1}[0-9]{1,6}[)])?([0-9 .-/]{3,20})((x|ext|extension)[ ]?[0-9]{1,4})?$""")
      if (isPhoneNumber) {
        Valid
      } else {
        Invalid(ValidationError("error.phone"))
      }
  })

  val form = Form(
    mapping(
      "phone-new" -> text.verifying(phoneCheckConstraint)
    )(Data.apply)(Data.unapply)
  )

  /**
   * The form data.
   *
   * @param phoneNew The new phone number of the user.
   */
  case class Data(
    phoneNew: String
  )
}
