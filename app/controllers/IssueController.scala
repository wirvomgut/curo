package controllers

import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import forms.IssueAddForm
import javax.inject.Inject
import models.common.Person
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.KanboardService
import utils.auth.DefaultEnv
import views.logic.IssueSystemViewLogic

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class IssueController @Inject()(
    kanboardService: KanboardService,
    cc: ControllerComponents,
    silhouette: Silhouette[DefaultEnv],
    actorSystem: ActorSystem
  )
  extends AbstractController(cc) with I18nSupport {

  private implicit val ec: ExecutionContext = actorSystem.dispatcher

  private val logger = Logger(getClass)

  val itemsPerPage = 9

  /**
    * Handles the issue system landing page action.
    *
    * @return The result to display.
    */
  def landing: Action[AnyContent] = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    for {
      viewLogic <- IssueSystemViewLogic.create(request.identity, kanboardService)
    } yield Ok(views.html.issuesystem(request.identity, viewLogic, IssueAddForm.form))
  }

  /**
    * Add a coin entry.
    *
    * @return The result to display.
    */
  def add: Action[AnyContent] = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    IssueAddForm.form.bindFromRequest.fold(
      form => {
        for {
          viewLogic <- IssueSystemViewLogic.create(request.identity, kanboardService)
        } yield BadRequest(views.html.issuesystem(request.identity, viewLogic, form))
      },
      data => {
        val person: Person = Person.findOrCreateByUid(request.identity.loginInfo.providerKey)

        logger.warn(data.toString)

        kanboardService.createTask(data.title, data.description, data.area, data.kind, person.uid)
          .map(_ => Redirect(routes.IssueController.landing()))
      })
  }
}
