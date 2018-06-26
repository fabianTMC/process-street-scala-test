package utilities

import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._

import javax.inject._

import authentikat.jwt._
import utilities.JWT

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

case class User(uuid: String) 
case class UserRequest[A](val userInfo: User, val request: Request[A]) extends WrappedRequest[A](request)

class JWTAuthentication @Inject() (parser: BodyParsers.Default)(implicit ec: ExecutionContext) extends ActionBuilder[UserRequest, AnyContent] {
   override protected def executionContext: ExecutionContext = ec
   override def parser: BodyParser[AnyContent] = parser

   override def invokeBlock[A](request: Request[A], block: (UserRequest[A]) => Future[Result]): Future[Result] = {
    implicit val req = request

    var jwtToken = request.headers.get("Authorization").getOrElse("")
    if(jwtToken.indexOf("Bearer ") == 0) {
        jwtToken = jwtToken.substring(7)
    }

    if (JWT.isValidToken(jwtToken)) {
      JWT.decodePayload(jwtToken).fold(
        Future(Unauthorized(Json.stringify(Json.obj(("success", false), ("error", "Invalid credentials")))).as("application/json"))
      ) { payload =>
        implicit val userFormat = Json.format[User]
        val userInfo = Json.parse(payload).validate[User].get

        // Replace this block with data source
        if (true) {
          block(new UserRequest[A](userInfo, request))
        } else {
          Future(Unauthorized(Json.stringify(Json.obj(("success", false), ("error", "Invalid credentials")))).as("application/json"))
        }
      }
    } else {
      Future(Unauthorized(Json.stringify(Json.obj(("success", false), ("error", "Invalid credentials")))).as("application/json"))
    }
  }
}