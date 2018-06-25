import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatest.concurrent.ScalaFutures

import models._

import org.scalatest.time.{Millis, Seconds, Span}

/**
 * Unit tests can run without a full Play application.
 */
class UsersModelSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures {
  implicit val defaultPatience =
  PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  def usersModel: UsersModel = app.injector.instanceOf(classOf[UsersModel])

  "UsersModel" should {

    "create a new user" in {
      whenReady(usersModel.create(Users(email = "test@test.com", password = "password"))) { result =>
        whenReady(usersModel.findAll()) { users =>
          users.size must equal(1)
          users(0).uuid must not be empty
          users(0).email must equal("test@test.com")
          users(0).password must not equal("password")
          users(0).salt must not equal("")
          users(0).alg must equal("SHA-256")
          users(0).verificationHash must not equal("")
          users(0).verified must equal(false)
          users(0).createdOn must not equal(None)
          users(0).updatedOn must equal(None)
          users(0).verifiedOn must equal(None)
        }
      }
    }

    "fail to create a new user because of a duplicate email address" in {
      whenReady(usersModel.create(Users(email = "test@test.com", password = "password"))) { result =>
        result must equal(Some(-1))
      }
    }
  }
  
}
