import akka.actor.ActorSystem
import controllers.{AsyncController}
import org.scalatestplus.play._
import play.api.test.Helpers._
import play.api.test.FakeRequest

/**
 * Unit tests can run without a full Play application.
 */
class UnitSpec extends PlaySpec {

  "AsyncController" should {

    "return a valid result on action.async" in {
      // actor system will create threads that must be cleaned up even if test fails
      val actorSystem = ActorSystem("test")
      try {
        implicit val ec = actorSystem.dispatcher
        val controller = new AsyncController(stubControllerComponents(), actorSystem)
        val resultFuture = controller.message(FakeRequest())
        contentAsString(resultFuture) must be("Hi!")
      } finally {
        // always shut down actor system at the end of the test.
        actorSystem.terminate()
      }
    }

  }

}
