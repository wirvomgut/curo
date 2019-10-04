package services

import org.scalatest.FunSuite
import play.api.libs.json.Json
import services.KanboardResponse.KanboardGetAllCategoriesResponse

class KanboardServiceTest extends FunSuite {
  import KanboardRequests.Implicits._
  import KanboardRequests._
  import KanboardResponse.Implicits._

  test("createTask request Json") {
    val jsonRaw = KanboardCreateTaskRequest(KanboardCreateTaskParams(
      title = "Test",
      description = "",
      project_id = 1,
      category_id = 2,
      swimlane_id = 3
    )).toJson

    println(jsonRaw)

    val request = Json.parse(jsonRaw).as[KanboardCreateTaskRequest]

    assert(request.jsonrpc == "2.0")
    assert(request.method == "createTask")
    assert(request.params.title == "Test")
    assert(request.params.project_id == 1)
    assert(request.params.category_id == 2)
    assert(request.params.swimlane_id == 3)
  }

  test("getAllCategories request Json") {
    val jsonRaw = KanboardGetAllCategoriesRequest(
      KanboardProjectIdParams(1)
    ).toJson

    val request = Json.parse(jsonRaw).as[KanboardGetAllCategoriesRequest]

    assert(request.jsonrpc == "2.0")
    assert(request.method == "getAllCategories")
    assert(request.params.project_id == 1)
  }

  test("getAllCategories response Json") {
    val jsonRaw = """{
                    |    "jsonrpc": "2.0",
                    |    "id": 1261777968,
                    |    "result": [
                    |        {
                    |            "id": "1",
                    |            "name": "Super category",
                    |            "project_id": "1"
                    |        }
                    |    ]
                    |}""".stripMargin.trim

    val request = Json.parse(jsonRaw).as[KanboardGetAllCategoriesResponse]

    assert(request.jsonrpc == "2.0")
    assert(request.id == 1261777968)
    assert(request.result.size == 1)
    assert(request.result.head.id == "1")
    assert(request.result.head.name == "Super category")
    assert(request.result.head.project_id == "1")
  }

}
