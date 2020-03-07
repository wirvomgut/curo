package controllers

import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import forms.IssueAddForm
import javax.inject.Inject
import models.common.Person
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import services.{KanboardService, PushoverPriority, PushoverService}
import utils.auth.DefaultEnv
import views.logic.IssueSystemViewLogic

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class IssueController @Inject()(
    kanboardService: KanboardService,
    pushoverService: PushoverService,
    cc: ControllerComponents,
    silhouette: Silhouette[DefaultEnv],
    actorSystem: ActorSystem
  )
  extends AbstractController(cc) with I18nSupport with Logging {

  private implicit val ec: ExecutionContext = actorSystem.dispatcher

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
        logger.info("Creating new issue...")

        val createTaskResponse = kanboardService
          .createTask(data.title, data.description, data.area, data.kind, person.uid, data.alarm)

        createTaskResponse.onComplete {
          case Success(x) =>
            logger.info("New issue successfully created.")
            if(data.alarm) {
              logger.info("Issue has high priority! Sending notification!")
              pushoverService.sendMessage(
                title = data.title,
                message = data.description,
                priority = PushoverPriority.LOUD_CONFIRM_NOTIFICATION,
                url = "https://" + kanboardService.url + "/project/" + kanboardService.projectId + "/task/" + x.result
              ).onComplete {
                case Success(x) =>
                  if(x.status == 200) logger.info("Notification for issue was successfully send.")
                  else logger.warn("Notification send but got wrong status code: " + x.status + " text " + x.statusText)
                case Failure(e) => logger.error("Error while sending issue notification.", e)
              }
            }
          case Failure(e) =>
            logger.error("Error while creating Kanboard task.", e)
        }

        createTaskResponse
          .map(_ => Redirect(routes.IssueController.landing()))
      })
  }
}
