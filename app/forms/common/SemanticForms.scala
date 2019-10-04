package forms.common

import views.html

/**
 * Created by julianliebl on 30.04.17.
 */
object SemanticForms {
  import views.html.helper.FieldConstructor
  implicit val input: FieldConstructor = FieldConstructor(html.form.forminput.f)
  val checkbox = FieldConstructor(html.form.formcheckbox.f)
}
