package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.api.{ Environment, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.{ CredentialsProvider, SocialProviderRegistry }
import forms.{ EmailForm, NoPasswordForm, PasswordForm, PhoneForm }
import models.User
import models.services.UserService
import play.api.Configuration
import play.api.Play.current
import play.api.cache.Cache
import play.api.i18n.{ Messages, MessagesApi }
import play.api.mvc.Action
import services.LdapClient

import scala.concurrent.Future
import scala.language.postfixOps

/**
 * The credentials auth controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 * @param userService The user service implementation.
 * @param authInfoRepository The auth info repository implementation.
 * @param credentialsProvider The credentials provider.
 * @param socialProviderRegistry The social provider registry.
 * @param configuration The Play configuration.
 * @param clock The clock instance.
 */
class LdapController @Inject() (
  val ldapClient: LdapClient,
  val messagesApi: MessagesApi,
  val env: Environment[User, CookieAuthenticator],
  userService: UserService,
  authInfoRepository: AuthInfoRepository,
  credentialsProvider: CredentialsProvider,
  socialProviderRegistry: SocialProviderRegistry,
  configuration: Configuration,
  clock: Clock)
  extends Silhouette[User, CookieAuthenticator] {

  /**
   * Changes the users password if form data is correct.
   *
   * @return The result to display.
   */
  def password = SecuredAction.async { implicit request =>
    PasswordForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.password(request.identity, form))),
      data => {
        val passwordOld = data.passwordOld
        val passwordNew = data.passwordNew
        val passwordConfirm = data.passwordConfirm
        val uid = request.identity.loginInfo.providerKey

        if (!ldapClient.authUser(uid, passwordOld)) {
          Future.successful(
            Redirect(routes.ApplicationController.password()).flashing("error" -> Messages("password.old.error"))
          )
        } else {
          if (passwordNew.equals(passwordConfirm) && !passwordNew.isEmpty) {
            ldapClient.modifyPassword(uid, passwordNew)

            Future.successful(
              Redirect(routes.ApplicationController.password()).flashing("success" -> Messages("password.save.success"))
            )
          } else {
            Future.successful(
              Redirect(routes.ApplicationController.password()).flashing("error" -> Messages("password.new.error"))
            )
          }
        }
      }
    )
  }

  /**
   * Checks if users password is missing.
   *
   * @return The result to display.
   */
  def nopasswordcheck(uid: String) = Action.async { implicit request =>
    if (uid.isEmpty || ldapClient.getUser(uid).isEmpty || ldapClient.hasPassword(uid)) {
      Future.successful(
        Redirect(routes.ApplicationController.signIn())
          .withHeaders("uid" -> uid)
          .flashing("error" -> Messages("invalid.credentials"))
      )
    } else {
      Future.successful(
        Redirect(routes.ApplicationController.nopassword(uid))
      )
    }
  }

  /**
   * Changes the missing users password if form data is correct.
   *
   * @return The result to display.
   */
  def nopassword = Action.async { implicit request =>
    NoPasswordForm.form.bindFromRequest.fold(
      form => {
        Future.successful(BadRequest(views.html.nopassword(form, form.data.getOrElse("uid", ""))))
      },
      data => {
        val uid = data.uid
        if (ldapClient.hasPassword(uid)) {
          Future.successful(
            Redirect(routes.ApplicationController.signIn()).flashing("error" -> Messages("invalid.credentials"))
          )
        } else {
          val passwordNew = data.passwordNew
          val passwordConfirm = data.passwordConfirm

          if (passwordNew.equals(passwordConfirm) && !passwordNew.isEmpty) {
            ldapClient.modifyPassword(uid, passwordNew)

            Future.successful(
              Redirect(routes.ApplicationController.signIn()).flashing("success" -> Messages("password.save.success"))
            )
          } else {
            Future.successful(
              Redirect(routes.ApplicationController.nopassword(uid)).flashing("error" -> Messages("password.new.error"))
            )
          }
        }
      }
    )
  }

  /**
   * Changes the users email if form data is correct.
   *
   * @return The result to display.
   */
  def email = SecuredAction.async { implicit request =>
    EmailForm.form.bindFromRequest.fold(
      form => Future.successful(
        BadRequest(views.html.email(request.identity, form))
      ),
      data => {
        ldapClient.modifyMail(request.identity.loginInfo.providerKey, data.emailNew)

        Future.successful(
          Redirect(routes.ApplicationController.email()).flashing("success" -> Messages("email.save.success"))
        )
      }
    )
  }

  /**
   * Changes the users email if form data is correct.
   *
   * @return The result to display.
   */
  def phone = SecuredAction.async { implicit request =>
    PhoneForm.form.bindFromRequest.fold(
      form => Future.successful(
        BadRequest(views.html.phone(request.identity, form))
      ),
      data => {
        ldapClient.modifyPhone(request.identity.loginInfo.providerKey, data.phoneNew)

        Future.successful(
          Redirect(routes.ApplicationController.phone()).flashing("success" -> Messages("phone.save.success"))
        )
      }
    )
  }
}