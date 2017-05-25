package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import forms.CoinAddForm
import models.User
import models.coin.{Person, WorkEntry}
import models.common.Pagination
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.Future


class CoinController @Inject()(
                                val messagesApi: MessagesApi,
                                val env: Environment[User, CookieAuthenticator]
                              ) extends Silhouette[User, CookieAuthenticator] {

  val itemsPerPage = 9

  /**
    * Handles the coin system landing page action.
    *
    * @return The result to display.
    */
  def landing: Action[AnyContent] = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.coinsystem(request.identity, CoinsystemViewLogic(request.identity), CoinAddForm.form)))
  }

  /**
    * Handles the coin system archive action.
    *
    * @return The result to display.
    */
  def archive(page: Int): Action[AnyContent] = SecuredAction.async { implicit request =>
    val logic = CoinsystemViewLogic(request.identity, Pagination(itemsPerPage, page * itemsPerPage))
    Future.successful(Ok(views.html.coinsystem(request.identity, logic, CoinAddForm.form)))
  }

  /**
    * Add a coin entry.
    *
    * @return The result to display.
    */
  def add: Action[AnyContent] = SecuredAction.async { implicit request =>
    CoinAddForm.form.bindFromRequest.fold(
      form => {
        Future.successful(BadRequest(views.html.coinsystem(request.identity, CoinsystemViewLogic(request.identity), form)))
      },
      data => {
        val person: Person = Person.findOrCreateByUid(request.identity.loginInfo.providerKey)
        WorkEntry.create(person.id, data.area, data.task, data.description, data.time, data.coin, data.date)
        Future.successful(Redirect(routes.CoinController.landing()))
      }
    )
  }

  /**
    * Remove a coin entry.
    *
    * @return The result to display.
    */
  def remove(id: Long): Action[AnyContent] = SecuredAction.async { implicit request =>
    WorkEntry.remove(id)
    Future.successful(Redirect(routes.CoinController.landing()))
  }
}
