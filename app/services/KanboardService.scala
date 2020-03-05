package services

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import monix.execution.Scheduler.{global => scheduler}
import monix.execution.atomic.Atomic
import okhttp3._
import play.api.libs.json.{Format, JsValue, Json}
import play.api.{Configuration, Logger}
import services.KanboardResponse._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


@Singleton
class KanboardService @Inject()(configuration: Configuration, actorSystem: ActorSystem, pushoverService: PushoverService) {
  import KanboardRequests._
  import KanboardResponse.Implicits._

  val logger: Logger = Logger(this.getClass)

  private implicit val ec: ExecutionContext = actorSystem.dispatcher

  val client = new OkHttpClient()

  val protocol: String = configuration.get[String]("curo.protocol")
  val url: String = configuration.get[String]("app.kanboard.url")
  val user: String = configuration.get[String]("kanboard.user")
  val pass: String = configuration.get[String]("kanboard.pass")
  val projectId: Int = configuration.get[Int]("kanboard.project.id")

  val credentials: String = Credentials.basic(user, pass)

  val JSON: MediaType = MediaType.get("application/json; charset=utf-8")

  def body(request: JsonRPCRequest[_]): RequestBody = {
    RequestBody.create(JSON, request.toJson)
  }

  def request(request: JsonRPCRequest[_]): Request = new Request.Builder()
    .url(protocol + url + "/jsonrpc.php")
    .header("Authorization", credentials)
    .post(body(request))
    .build()

  private val _categories = Atomic(Seq.empty[KanboardCategory])
  def categories: Seq[KanboardCategory] = _categories.get

  private val _swimlanes = Atomic(Seq.empty[KanboardSwimlane])
  def swimlanes: Seq[KanboardSwimlane] = _swimlanes.get



  def createTask(title: String, desc: String, area: String, kind: String, reporter: String, alarm: Boolean): Future[KanboardCreateTaskResponse] = {
    def categoryId: Int = area.toInt
    def swimlaneId: Int = kind.toInt

    Future {
      client.newCall(
        request(KanboardCreateTaskRequest(KanboardCreateTaskParams(
          title = title,
          description = desc,
          project_id = projectId,
          category_id = categoryId,
          swimlane_id = swimlaneId,
          tags = if(alarm) Array(reporter, "alarm") else Array(reporter)
        )))
      ).execute()
    }.map {
      r => Json.parse(r.body.string()).as[KanboardCreateTaskResponse]
    }
  }

  def tasksReportedBy(reporter: String): Future[Seq[KanboardTask]] = {
    Future {
      client.newCall(
        request(KanboardSearchTasksRequest(KanboardSearchTasksParams(
          project_id = projectId,
          query = "tag:" + reporter
        )))
      ).execute()
    }.map {
      r => Try(Json.parse(r.body.string()).as[KanboardTasksResponse].result).getOrElse(Seq.empty)
    }
  }

  import concurrent.duration._
  scheduler.scheduleWithFixedDelay(0.seconds, 5.minutes) {
    refreshCategories()
    refreshSwimlanes()
  }

  private def refreshCategories(): Unit = {
    Future {
      client.newCall(
        request(KanboardGetAllCategoriesRequest(KanboardProjectIdParams(projectId)))
      ).execute()
    }.onComplete {
      case Success(r) =>
        val maybeBody = Try(r.body.string())
        val maybeResponse = maybeBody.map(s => Json.parse(s).as[KanboardGetAllCategoriesResponse])

        maybeResponse.map(_.result).foreach(_categories.set)

        maybeResponse.failed.foreach(logger.error(s"Parsing of all categories request failed: $maybeBody", _))
      case Failure(e) =>
        logger.error("Request to get all categories failed.", e)
    }
  }

  private def refreshSwimlanes(): Unit = {
    Future {
      client.newCall(
        request(KanboardGetAllSwimlanesRequest(KanboardProjectIdParams(projectId)))
      ).execute()
    }.onComplete {
      case Success(r) =>
        val maybeBody = Try(r.body.string())
        val maybeResponse = maybeBody.map(s => Json.parse(s).as[KanboardGetAllSwimlanesResponse])

        maybeResponse.map(_.result).foreach(_swimlanes.set)

        maybeResponse.failed.foreach(logger.error(s"Parsing of all swimlanes request failed: $maybeBody", _))
      case Failure(e) =>
        logger.error("Request to get all swimlanes failed.", e)
    }
  }
}

object KanboardRequests {
  object Implicits {
    implicit val kanboardCreateTaskParamsWrites:Format[KanboardCreateTaskParams] = Json.format[KanboardCreateTaskParams]
    implicit val kanboardCreateTaskRequestWrites:Format[KanboardCreateTaskRequest] = Json.format[KanboardCreateTaskRequest]

    implicit val kanboardSearchTasksParamsWrites:Format[KanboardSearchTasksParams] = Json.format[KanboardSearchTasksParams]
    implicit val kanboardSearchTasksRequestWrites:Format[KanboardSearchTasksRequest] = Json.format[KanboardSearchTasksRequest]

    implicit val kanboardProjectIdParamsWrites:Format[KanboardProjectIdParams] = Json.format[KanboardProjectIdParams]
    implicit val kanboardGetAllCategoriesWrites:Format[KanboardGetAllCategoriesRequest] = Json.format[KanboardGetAllCategoriesRequest]
    implicit val kanboardGetAllSwimlanesRequestWrites:Format[KanboardGetAllSwimlanesRequest] = Json.format[KanboardGetAllSwimlanesRequest]
  }

  import Implicits._

  case class KanboardProjectIdParams(project_id: Int)

  case class KanboardCreateTaskParams(title: String, description: String, project_id: Int, swimlane_id: Int, category_id: Int, tags: Array[String] = Array.empty)
  case class KanboardCreateTaskRequest(params: KanboardCreateTaskParams) extends JsonRPCRequest[KanboardCreateTaskRequest] {
    override val method: String = "createTask"

    override protected def _toJson: JsValue = Json.toJson(this)
  }

  case class KanboardSearchTasksParams(project_id: Int, query: String)
  case class KanboardSearchTasksRequest(params: KanboardSearchTasksParams) extends JsonRPCRequest[KanboardSearchTasksRequest] {
    override val method: String = "searchTasks"

    override protected def _toJson: JsValue = Json.toJson(this)
  }

  case class KanboardGetAllCategoriesRequest(params: KanboardProjectIdParams) extends JsonRPCRequest[KanboardGetAllCategoriesRequest] {
    override val method: String = "getAllCategories"

    override protected def _toJson: JsValue = Json.toJson(this)
  }

  case class KanboardGetAllSwimlanesRequest(params: KanboardProjectIdParams) extends JsonRPCRequest[KanboardGetAllSwimlanesRequest] {
    override val method: String = "getAllSwimlanes"

    override protected def _toJson: JsValue = Json.toJson(this)
  }
}

object KanboardResponse {
  object Implicits {
    import ai.x.play.json.Jsonx
    import ai.x.play.json.Encoders._

    implicit val kanboardCategoryFormat:Format[KanboardCategory] = Json.format[KanboardCategory]
    implicit val kanboardSwimlaneFormat:Format[KanboardSwimlane] = Json.format[KanboardSwimlane]
    implicit val kanboardTaskFormat:Format[KanboardTask] = Jsonx.formatCaseClass[KanboardTask]
    implicit val kanboardCreateTaskResponseFormat:Format[KanboardCreateTaskResponse] = Json.format[KanboardCreateTaskResponse]
    implicit val kanboardSearchTasksResponseFormat:Format[KanboardTasksResponse] = Json.format[KanboardTasksResponse]
    implicit val kanboardGetAllCategoriesResponseFormat:Format[KanboardGetAllCategoriesResponse] = Json.format[KanboardGetAllCategoriesResponse]
    implicit val kanboardGetAllSwimlanesResponseFormat:Format[KanboardGetAllSwimlanesResponse] = Json.format[KanboardGetAllSwimlanesResponse]
  }

  case class KanboardCategory(id: String, name: String, project_id: String)
  case class KanboardSwimlane(id: String, name: String, position: String, is_active: String, project_id: String)

  case class KanboardCreateTaskResponse(jsonrpc: String, id: Long, result: Int)
  case class KanboardGetAllCategoriesResponse(jsonrpc: String, id: Long, result: Seq[KanboardCategory])
  case class KanboardGetAllSwimlanesResponse(jsonrpc: String, id: Long, result: Seq[KanboardSwimlane])
  case class KanboardTasksResponse(jsonrpc: String, id: Long, result: Seq[KanboardTask])

  case class KanboardTask(
    nb_comments: Option[String],
    nb_files: Option[String],
    nb_subtasks: Option[String],
    nb_completed_subtasks: Option[String],
    nb_links: Option[String],
    nb_external_links: Option[String],
    is_milestone: Option[String],
    id: Option[String],
    reference: Option[String],
    title: Option[String],
    description: Option[String],
    date_creation: Option[String],
    date_modification: String,
    date_completed: Option[String],
    date_started: Option[String],
    date_due: Option[String],
    color_id: Option[String],
    project_id: Option[String],
    column_id: Option[String],
    swimlane_id: Option[String],
    owner_id: Option[String],
    creator_id: Option[String],
    position: Option[String],
    is_active: Option[String],
    score: Option[String],
    category_id: Option[String],
    priority: Option[String],
    date_moved: Option[String],
    recurrence_status: Option[String],
    recurrence_trigger: Option[String],
    recurrence_factor: Option[String],
    recurrence_timeframe: Option[String],
    recurrence_basedate: Option[String],
    recurrence_parent: Option[String],
    recurrence_child: Option[String],
    time_estimated: Option[String],
    time_spent: Option[String],
    assignee_username: Option[String],
    assignee_name: Option[String],
    assignee_email: Option[String],
    assignee_avatar_path: Option[String],
    category_name: Option[String],
    category_description: Option[String],
    category_color_id: Option[String],
    column_name: Option[String],
    column_position: Option[String],
    swimlane_name: Option[String],
    project_name: Option[String]
  )
}

trait JsonRPCRequest[T <: JsonRPCRequest[T]] {
  val jsonrpc: String = "2.0"
  val method: String
  val id: Long = System.currentTimeMillis


  protected def stringify(json: JsValue): String = addify(Json.stringify(json))
  private def addify(json: String): String = s"""{"jsonrpc":"$jsonrpc", "id":$id, "method":"$method", """ + json.tail

  protected def _toJson: JsValue
  def toJson: String = stringify(_toJson)
}