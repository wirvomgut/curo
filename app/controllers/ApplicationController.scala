package controllers

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.{ LogoutEvent, Silhouette }
import forms._
import javax.inject.Inject
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents }
import services.LdapClient
import utils.auth.DefaultEnv

import scala.concurrent.Future

//noinspection TypeAnnotation
class ApplicationController @Inject() (
  cc: ControllerComponents,
  ldapClient: LdapClient,
  configuration: Configuration,
  silhouette: Silhouette[DefaultEnv])
  extends AbstractController(cc) with I18nSupport {

  /**
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.home(request.identity)))
  }

  /**
   * Handles the profile action.
   *
   * @return The result to display.
   */
  def profile = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.profile(request.identity)))
  }

  /**
   * Handles the password action.
   *
   * @return The result to display.
   */
  def password = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.password(request.identity, PasswordForm.form)))
  }

  /**
   * Handles the no password action.
   *
   * @return The result to display.
   */
  def nopassword(uid: String) = silhouette.UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(_) => Future.successful(Redirect(routes.ApplicationController.index()))
      case None => Future.successful(Ok(views.html.nopassword(NoPasswordForm.form, uid)))
    }
  }

  /**
   * Handles the email action.
   *
   * @return The result to display.
   */
  def email = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.email(request.identity, EmailForm.form)))
  }

  /**
   * Handles the phone number action.
   *
   * @return The result to display.
   */
  def phone = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.phone(request.identity, PhoneForm.form)))
  }

  /**
   * Handles the directory action.
   *
   * @return The result to display.
   */
  def directory = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.directory(request.identity, ldapClient.getUsers())))
  }

  val protocol: String = configuration.get[String]("curo.protocol")
  val nextcloudUrl: String = configuration.get[String]("app.nextcloud.url")

  /**
   * Handles the files action.
   *
   * @return The result to display.
   */
  val nextcloudFilesPath: String = configuration.get[String]("app.nextcloud.files.path")
  def files = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.iframe(request.identity, protocol + nextcloudUrl + nextcloudFilesPath)))
  }

  /**
   * Handles the calendar action.
   *
   * @return The result to display.
   */
  val nextcloudCalendarPath: String = configuration.get[String]("app.nextcloud.calendar.path")
  def calendar = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.iframe(request.identity, protocol + nextcloudUrl + nextcloudCalendarPath)))
  }

  /**
   * Handles the carsharing action.
   *
   * @return The result to display.
   */
  val nextcloudCarSharingPath: String = configuration.get[String]("app.nextcloud.carsharing.path")
  def carsharing = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.iframe(request.identity, protocol + nextcloudUrl + nextcloudCarSharingPath)))
  }

  /**
   * Handles the forum action.
   *
   * @return The result to display.
   */
  val forumUrl: String = configuration.get[String]("app.discourse.url")
  def forum = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.iframe(request.identity, protocol + forumUrl)))
  }

  /**
   * Handles the kanban action.
   *
   * @return The result to display.
   */
  val kanbanUrl: String = configuration.get[String]("app.kanboard.url")
  def kanban(path: String = "") = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.iframe(request.identity, protocol + kanbanUrl)))
  }

  /**
   * Handles the chat action.
   *
   * @return The result to display.
   */
  val chatUrl: String = configuration.get[String]("app.rocketchat.url")
  def chat = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.iframe(request.identity, protocol + chatUrl)))
  }

  /**
   * Handles the Sign In action.
   *
   * @return The result to display.
   */
  def signIn = silhouette.UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(_) => Future.successful(Redirect(routes.ApplicationController.index()))
      case None => Future.successful(Ok(views.html.signIn(SignInForm.form, request.headers.get("uid").getOrElse(""))))
    }
  }

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    val result = Redirect(routes.ApplicationController.index())
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))

    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }
}
