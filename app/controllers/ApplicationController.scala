package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import forms._
import models.User
import play.api.Configuration
import play.api.i18n.MessagesApi
import services.LdapClient

import scala.concurrent.Future

/**
 * The basic application controller.
 *
 * @param messagesApi The Play messages API.
 * @param env The Silhouette environment.
 */
class ApplicationController @Inject() (
  val ldapClient: LdapClient,
  val messagesApi: MessagesApi,
  val configuration: Configuration,
  val socialProviderRegistry: SocialProviderRegistry,
  val env: Environment[User, CookieAuthenticator])
  extends Silhouette[User, CookieAuthenticator] {

  /**
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.home(request.identity)))
  }

  /**
    * Handles the profile action.
    *
    * @return The result to display.
    */
  def profile = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.profile(request.identity)))
  }

  /**
    * Handles the password action.
    *
    * @return The result to display.
    */
  def password = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.password(request.identity, PasswordForm.form)))
  }

  /**
    * Handles the no password action.
    *
    * @return The result to display.
    */
  def nopassword(uid:String) = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Redirect(routes.ApplicationController.index()))
      case None => Future.successful(Ok(views.html.nopassword(NoPasswordForm.form, uid)))
    }
  }

  /**
    * Handles the email action.
    *
    * @return The result to display.
    */
  def email = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.email(request.identity, EmailForm.form)))
  }

  /**
    * Handles the phone number action.
    *
    * @return The result to display.
    */
  def phone = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.phone(request.identity, PhoneForm.form)))
  }

  /**
    * Handles the directory action.
    *
    * @return The result to display.
    */
  def directory = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.directory(request.identity, ldapClient.getUsers())))
  }

  /**
    * Handles the calendar action.
    *
    * @return The result to display.
    */
  def calendar = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.iframe(request.identity, "https://cloud.wirvomgut.de/index.php/apps/calendar")))
  }

  /**
    * Handles the files action.
    *
    * @return The result to display.
    */
  val nextcloudUrl: String = configuration.getString("nextcloud.url").getOrElse("http://localhost")
  def files = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.iframe(request.identity, "https://cloud.wirvomgut.de/index.php/apps/files/")))
  }

  /**
    * Handles the carsharing action.
    *
    * @return The result to display.
    */
  def carsharing = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.iframe(request.identity, "https://cloud.wirvomgut.de/index.php/apps/external/1")))
  }

  /**
    * Handles the forum action.
    *
    * @return The result to display.
    */
  def forum = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.iframe(request.identity, "https://forum.wirvomgut.de/")))
  }

  /**
    * Handles the chat action.
    *
    * @return The result to display.
    */
  def chat = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.iframe(request.identity, "https://chat.wvg.io")))
  }

  /**
   * Handles the Sign In action.
   *
   * @return The result to display.
   */
  def signIn = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Redirect(routes.ApplicationController.index()))
      case None => Future.successful(Ok(views.html.signIn(SignInForm.form, request.headers.get("uid").getOrElse(""))))
    }
  }

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut = SecuredAction.async { implicit request =>
    val result = Redirect(routes.ApplicationController.index())
    env.eventBus.publish(LogoutEvent(request.identity, request, request2Messages))

    env.authenticatorService.discard(request.authenticator, result)
  }
}
