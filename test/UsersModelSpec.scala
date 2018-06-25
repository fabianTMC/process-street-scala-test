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
        }
      }
    }
  }
  
}
