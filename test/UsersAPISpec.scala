
import controllers.UsersController
import org.scalatest.concurrent.ScalaFutures
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test.CSRFTokenHelper._

import play.api.libs.json._
import models._

class FunctionalSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures {

  def usersController = app.injector.instanceOf(classOf[UsersController])
  implicit val payloadRead = Json.reads[Users]

  "create()" should {
        "fail to create a new user since there is no payload" in {
            val result = usersController.create(FakeRequest())

            val jsonResult = Json.parse(contentAsString(result))
            (jsonResult \ "success").as[Boolean] must equal(false)
            (jsonResult \ "error").as[String] must equal("Invalid request body")

            status(result) must equal(BAD_REQUEST)
        }

        "fail to create a new user since there is an empty payload" in {
            val result = usersController.create(FakeRequest()withJsonBody(Json.obj()))

            val jsonResult = Json.parse(contentAsString(result))
            (jsonResult \ "success").as[Boolean] must equal(false)
            (jsonResult \ "error").as[String] must equal("Fields are missing from the JSON body")

            status(result) must equal(BAD_REQUEST)
        }

        "fail to create a new user since there is only a email in the payload" in {
            val result = usersController.create(FakeRequest()withJsonBody(Json.obj("email" -> "test@test.com")))

            val jsonResult = Json.parse(contentAsString(result))
            (jsonResult \ "success").as[Boolean] must equal(false)
            (jsonResult \ "error").as[String] must equal("Fields are missing from the JSON body")

            status(result) must equal(BAD_REQUEST)
        }

        "fail to create a new user since there is only a password in the payload" in {
            val result = usersController.create(FakeRequest()withJsonBody(Json.obj("password" -> "password2")))

            val jsonResult = Json.parse(contentAsString(result))
            (jsonResult \ "success").as[Boolean] must equal(false)
            (jsonResult \ "error").as[String] must equal("Fields are missing from the JSON body")

            status(result) must equal(BAD_REQUEST)
        }

        "fail to create a new user since the password is common" in {
            val result = usersController.create(FakeRequest().withJsonBody(Json.obj("email" -> "test@test.com", "password" -> "password")))

            val jsonResult = Json.parse(contentAsString(result))
            (jsonResult \ "success").as[Boolean] must equal(false)
            (jsonResult \ "error").as[String] must equal("Invalid password provided")

            status(result) must equal(OK)
        }

        "fail to create a new user since the password is too short" in {
            val result = usersController.create(FakeRequest().withJsonBody(Json.obj("email" -> "test@test.com", "password" -> "pass")))

            val jsonResult = Json.parse(contentAsString(result))
            (jsonResult \ "success").as[Boolean] must equal(false)
            (jsonResult \ "error").as[String] must equal("Invalid password provided")

            status(result) must equal(OK)
        }

        "create a new user" in {
            val result = usersController.create(FakeRequest().withJsonBody(Json.obj("email" -> "test@test.com", "password" -> "test123")))

            val jsonResult = Json.parse(contentAsString(result))
            (jsonResult \ "success").as[Boolean] must equal(true)

            status(result) must equal(OK)
        }

        "fail to create a new user as the email address is already in the database" in {
            val result = usersController.create(FakeRequest().withJsonBody(Json.obj("email" -> "test@test.com", "password" -> "test123")))

            val jsonResult = Json.parse(contentAsString(result))
            (jsonResult \ "success").as[Boolean] must equal(false)
            (jsonResult \ "error").as[String] must equal("This email address is already signed up")

            status(result) must equal(OK)
        }
    }

    "index()" should {

        "list all users" in {
        val result = usersController.index(FakeRequest())
        val users = Json.parse(contentAsString(result)).as[Seq[Users]]

        status(result) must equal(OK)
        users.length must equal(1)
        }
    }

    "login()" should {
        "fail to login a user since there is no payload" in {
            val result = usersController.login(FakeRequest())

            val jsonResult = Json.parse(contentAsString(result))
            (jsonResult \ "success").as[Boolean] must equal(false)
            (jsonResult \ "error").as[String] must equal("Invalid request body")

            status(result) must equal(BAD_REQUEST)
        }

        "fail to login a user since there is an empty payload" in {
            val result = usersController.login(FakeRequest()withJsonBody(Json.obj()))

            val jsonResult = Json.parse(contentAsString(result))
            (jsonResult \ "success").as[Boolean] must equal(false)
            (jsonResult \ "error").as[String] must equal("Fields are missing from the JSON body")

            status(result) must equal(BAD_REQUEST)
        }

        "fail to login a user since there is only a email in the payload" in {
            val result = usersController.login(FakeRequest()withJsonBody(Json.obj("email" -> "test@test.com")))

            val jsonResult = Json.parse(contentAsString(result))
            (jsonResult \ "success").as[Boolean] must equal(false)
            (jsonResult \ "error").as[String] must equal("Fields are missing from the JSON body")

            status(result) must equal(BAD_REQUEST)
        }

        "fail to login a user since there is only a password in the payload" in {
            val result = usersController.login(FakeRequest()withJsonBody(Json.obj("password" -> "password2")))

            val jsonResult = Json.parse(contentAsString(result))
            (jsonResult \ "success").as[Boolean] must equal(false)
            (jsonResult \ "error").as[String] must equal("Fields are missing from the JSON body")

            status(result) must equal(BAD_REQUEST)
        }

        "fail to login a user as the email address does not exist" in {
            val result = usersController.login(FakeRequest().withJsonBody(Json.obj("email" -> "test1@test.com", "password" -> "test123")))

            val jsonResult = Json.parse(contentAsString(result))
            (jsonResult \ "success").as[Boolean] must equal(false)
            (jsonResult \ "error").as[String] must equal("Invalid login credentials")

            status(result) must equal(OK)
        }

        "fail to login a user as the password is incorrect" in {
            val result = usersController.login(FakeRequest().withJsonBody(Json.obj("email" -> "test@test.com", "password" -> "test1234asdasdasda")))

            val jsonResult = Json.parse(contentAsString(result))
            (jsonResult \ "success").as[Boolean] must equal(false)
            (jsonResult \ "error").as[String] must equal("Invalid login credentials")

            status(result) must equal(OK)
        }

        "login the user" in {
            val result = usersController.login(FakeRequest().withJsonBody(Json.obj("email" -> "test@test.com", "password" -> "test123")))

            val jsonResult = Json.parse(contentAsString(result))
            (jsonResult \ "success").as[Boolean] must equal(true)
            (jsonResult \ "token").as[String] must not be empty

            status(result) must equal(OK)
        }
    }
}
