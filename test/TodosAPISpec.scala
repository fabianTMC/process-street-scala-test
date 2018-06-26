
import controllers.TodoController
import org.scalatest.concurrent.ScalaFutures
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test.CSRFTokenHelper._

import play.api.libs.json._
import utilities._
import models._

class TodosAPISpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures {

  def usersModel: UsersModel = app.injector.instanceOf(classOf[UsersModel])
  def todoController = app.injector.instanceOf(classOf[TodoController])
  implicit val payloadRead = Json.reads[Users]

  "create()" should {
        "fail to create a new todo since there is no JWT token" in {
            val result = todoController.create(FakeRequest())

            val jsonResult = Json.parse(contentAsString(result))
            (jsonResult \ "success").as[Boolean] must equal(false)
            (jsonResult \ "error").as[String] must equal("Invalid credentials")

            status(result) must equal(UNAUTHORIZED)
        }
    }
}
