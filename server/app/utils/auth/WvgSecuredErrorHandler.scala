package utils.auth

import com.mohiva.play.silhouette.api.actions.SecuredErrorHandler
import controllers.routes
import javax.inject.Inject
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.mvc.Results._
import play.api.mvc.{ RequestHeader, Result }

import scala.concurrent.Future

/**
 * A secured error handler.
 */
class WvgSecuredErrorHandler @Inject() (val messagesApi: MessagesApi) extends SecuredErrorHandler with I18nSupport {

  /**
   * Called when a user is not authenticated.
   *
   * As defined by RFC 2616, the status code of the response should be 401 Unauthorized.
   *
   * @param request The request header.
   * @return The result to send to the client.
   */
  override def onNotAuthenticated(implicit request: RequestHeader): Future[Result] = {
    Future.successful(Redirect(routes.ApplicationController.signIn()))
  }

  /**
   * Called when a user is authenticated but not authorized.
   *
   * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
   *
   * @param request The request header.
   * @return The result to send to the client.
   */
  override def onNotAuthorized(implicit request: RequestHeader): Future[Result] = {
    Future.successful(Redirect(routes.ApplicationController.signIn()).flashing("error" -> Messages("access.denied")))
  }
}
