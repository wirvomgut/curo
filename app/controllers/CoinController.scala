package controllers

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import forms.CoinAddForm
import javax.inject.Inject
import models.coin.WorkEntry.WorkEntryId
import models.coin.WorkEntry
import models.common.{Pagination, Person}
import org.joda.time.DateTime
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import utils.auth.DefaultEnv
import views.logic.CoinsystemViewLogic

import scala.concurrent.Future

class CoinController @Inject() (
  cc: ControllerComponents,
  silhouette: Silhouette[DefaultEnv])
  extends AbstractController(cc) with I18nSupport {

  lazy val defaultLang: Lang = Lang(java.util.Locale.getDefault)

  val itemsPerPage = 9

  /**
   * Handles the coin system landing page action.
   *
   * @return The result to display.
   */
  def landing: Action[AnyContent] = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.coinsystem(request.identity, CoinsystemViewLogic(request.identity), CoinAddForm.form)))
  }

  /**
   * Handles the coin system archive action.
   *
   * @return The result to display.
   */
  def archive(page: Int): Action[AnyContent] = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    val logic = CoinsystemViewLogic(request.identity, Pagination(itemsPerPage, page * itemsPerPage))
    Future.successful(Ok(views.html.coinsystem(request.identity, logic, CoinAddForm.form)))
  }

  /**
   * Handles the coin system edit page action.
   *
   * @return The result to display.
   */
  def editPage(workEntryId: WorkEntryId): Action[AnyContent] = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful {
      WorkEntry
        .findById(workEntryId).map(CoinAddForm.fill)
        .map(data => {
          Ok(views.html.coin.editCoinEntry(request.identity, CoinsystemViewLogic(request.identity), CoinAddForm.form.fill(data), workEntryId))
        }).getOrElse(BadRequest)
    }
  }

  /**
   * Handles the coin system copy page action.
   *
   * @return The result to display.
   */
  def copyPage(workEntryId: WorkEntryId): Action[AnyContent] = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful {
      WorkEntry
        .findById(workEntryId).map(_.copy(dateDone = DateTime.now)).map(CoinAddForm.fill)
        .map(data => {
          Ok(views.html.coinsystem(request.identity, CoinsystemViewLogic(request.identity), CoinAddForm.form.fill(data)))
        }).getOrElse(BadRequest)
    }
  }

  /**
   * Add a coin entry.
   *
   * @return The result to display.
   */
  def add: Action[AnyContent] = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
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
          dateDone = data.date)

        Future.successful(Redirect(routes.CoinController.landing()))
      })
  }

  /**
   * Edit a coin entry.
   *
   * @return The result to display.
   */
  def edit(workEntryId: WorkEntryId): Action[AnyContent] = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
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
            dateDone = data.date))
        })

        Future.successful(Redirect(routes.CoinController.landing()))
      })
  }

  /**
   * Remove a coin entry.
   *
   * @return The result to display.
   */
  def remove(id: Long): Action[AnyContent] = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    WorkEntry.remove(id)
    Future.successful(Redirect(routes.CoinController.landing()))
  }
}
