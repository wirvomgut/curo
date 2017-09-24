package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import forms.CoinAddForm
import models.User
import models.coin.WorkEntry.WorkEntryId
import models.coin.{Person, WorkEntry}
import models.common.Pagination
import org.joda.time.DateTime
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
    * Handles the coin system edit page action.
    *
    * @return The result to display.
    */
  def editPage(workEntryId: WorkEntryId): Action[AnyContent] = SecuredAction.async { implicit request => Future.successful {
    WorkEntry
      .findById(workEntryId).map(CoinAddForm.fill)
      .map(data => {
        Ok(views.html.coin.editCoinEntry(request.identity, CoinsystemViewLogic(request.identity), CoinAddForm.form.fill(data), workEntryId))
      }).getOrElse(BadRequest)
  }}

  /**
    * Handles the coin system copy page action.
    *
    * @return The result to display.
    */
  def copyPage(workEntryId: WorkEntryId): Action[AnyContent] = SecuredAction.async { implicit request => Future.successful {
    WorkEntry
      .findById(workEntryId).map(_.copy(dateDone = DateTime.now)).map(CoinAddForm.fill)
      .map(data => {
        Ok(views.html.coinsystem(request.identity, CoinsystemViewLogic(request.identity), CoinAddForm.form.fill(data)))
      }).getOrElse(BadRequest)
  }}


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

        val areaAndDetail = CoinAddForm.areaValueNameToAreaDetail(data.area)

        WorkEntry.create(
          personId = person.id,
          kind = data.kind,
          area = areaAndDetail.area,
          areaDetail = areaAndDetail.detail,
          description = data.description,
          timeSpent = data.time,
          coins = data.coin,
          dateDone = data.date
        )

        Future.successful(Redirect(routes.CoinController.landing()))
      }
    )
  }

  /**
    * Edit a coin entry.
    *
    * @return The result to display.
    */
  def edit(workEntryId: WorkEntryId): Action[AnyContent] = SecuredAction.async { implicit request =>
    CoinAddForm.form.bindFromRequest.fold(
      form => {
        Future.successful(BadRequest(views.html.coinsystem(request.identity, CoinsystemViewLogic(request.identity), form)))
      },
      data => {
        val workEntry: Option[WorkEntry] = WorkEntry.findById(workEntryId)

        val areaAndDetail = CoinAddForm.areaValueNameToAreaDetail(data.area)

        workEntry.map(e => {
          WorkEntry.edit(e.copy(
            kind = data.kind,
            area = areaAndDetail.area,
            areaDetail = areaAndDetail.detail,
            description = data.description,
            timeSpent = data.time,
            coins = data.coin,
            dateDone = data.date
          ))
        })

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
