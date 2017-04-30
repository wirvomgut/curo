package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import forms.CoinAddForm
import models.User
import models.coin.{Person, WorkEntry}
import play.api.i18n.MessagesApi

import scala.concurrent.Future


class CoinController @Inject() (
                                 val messagesApi: MessagesApi,
                                 val env: Environment[User, CookieAuthenticator]
                               )extends Silhouette[User, CookieAuthenticator] {
  /**
    * Add a coin entry.
    *
    * @return The result to display.
    */
  def add = SecuredAction.async { implicit request =>
    CoinAddForm.form.bindFromRequest.fold(
      form => {
        val person = Person.findOrCreateByUid(request.identity.loginInfo.providerKey)
        val workEntries = Person.findWorkEntries(person.id)
        Future.successful(BadRequest(views.html.coinsystem(request.identity, workEntries, form)))
      },
      data => {
        val person: Person = Person.findOrCreateByUid(request.identity.loginInfo.providerKey)
        WorkEntry.create(person.id, data.area, data.description, data.time, data.coin, data.date)
        Future.successful(Redirect(routes.ApplicationController.coin()))
      }
    )
  }

  def remove(id: Long) = SecuredAction.async { implicit request =>
    WorkEntry.remove(id)
    Future.successful(Redirect(routes.ApplicationController.coin()))
  }
}
