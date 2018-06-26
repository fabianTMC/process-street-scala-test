package controllers

import javax.inject._

import play.api.mvc._
import models._
import utilities.JWTAuthentication
import utilities.JWTAuthentication

import play.api.libs.json._

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller handles all the operations that are made on the Users model
 */
@Singleton
class CommentsController @Inject()(cc: ControllerComponents, jwtAuthentication: JWTAuthentication, commentsModel: CommentsModel) (implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {
    implicit val commentsFormat = Json.format[Comments]

  /**
   * Create a comment
   */
  def create = jwtAuthentication.async { implicit request =>
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map { json =>
      try {
        val text = (json \ "text").as[String]
        val todo = (json \ "todo").as[String]

        commentsModel.create(Comments(text = text, belongsToTodo = todo, belongsToUser = request.userInfo.uuid)).flatMap {
          result => {
            if(result == Some(-2)) {
              Future(Ok(Json.stringify(Json.obj(("success", false), ("error", "Invalid user")))).as("application/json"))
            } else {
              Future(Ok(Json.stringify(Json.obj(("success", true)))).as("application/json"))
            }
          }
        }.recover {
          case e: Exception => {
            e.printStackTrace()
            InternalServerError(Json.stringify(Json.obj(("success", false), ("error", "An internal server error occurred")))).as("application/json")
          }
        }
      } catch {
        case e @ (_ : play.api.libs.json.JsResultException | _ : java.util.NoSuchElementException) => {
          Future(BadRequest(Json.stringify(Json.obj(("success", false), ("error", "Fields are missing from the JSON body")))).as("application/json"))
        }

        case e:Exception => {
          e.printStackTrace()
          Future(InternalServerError(Json.stringify(Json.obj(("success", false), ("error", "An internal server error occurred")))).as("application/json"))  
        }
      }
    }.getOrElse {
      Future(BadRequest(Json.stringify(Json.obj(("success", false), ("error", "Invalid request body")))).as("application/json"))
    }
  }

  /**
  * List all the non deleted comments belonging to the user
  */
  def index = jwtAuthentication.async { implicit request =>
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map { json =>
      try {
        val todo = (json \ "todo").as[String]

        commentsModel.findByTodo(todo).map { result =>
          Ok(Json.stringify(Json.toJson(result))).as("application/json")
        }
      } catch {
        case e @ (_ : play.api.libs.json.JsResultException | _ : java.util.NoSuchElementException) => {
          Future(BadRequest(Json.stringify(Json.obj(("success", false), ("error", "Fields are missing from the JSON body")))).as("application/json"))
        }

        case e:Exception => {
          e.printStackTrace()
          Future(InternalServerError(Json.stringify(Json.obj(("success", false), ("error", "An internal server error occurred")))).as("application/json"))  
        }
      }
    }.getOrElse {
      Future(BadRequest(Json.stringify(Json.obj(("success", false), ("error", "Invalid request body")))).as("application/json"))
    }
  }

  /**
   * Change the text of a comment that was not edited
   */
  def edit = jwtAuthentication.async { implicit request =>
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map { json =>
      try {
        val uuid = (json \ "uuid").as[String]
        val text = (json \ "text").as[String]

        commentsModel.edit(uuid, request.userInfo.uuid, text).flatMap {
          result => {
            Future(Ok(Json.stringify(Json.obj(("success", true)))).as("application/json"))
          }
        }.recover {
          case e: Exception => {
            e.printStackTrace()
            InternalServerError(Json.stringify(Json.obj(("success", false), ("error", "An internal server error occurred")))).as("application/json")
          }
        }
      } catch {
        case e @ (_ : play.api.libs.json.JsResultException | _ : java.util.NoSuchElementException) => {
          Future(BadRequest(Json.stringify(Json.obj(("success", false), ("error", "Fields are missing from the JSON body")))).as("application/json"))
        }

        case e:Exception => {
          e.printStackTrace()
          Future(InternalServerError(Json.stringify(Json.obj(("success", false), ("error", "An internal server error occurred")))).as("application/json"))  
        }
      }
    }.getOrElse {
      Future(BadRequest(Json.stringify(Json.obj(("success", false), ("error", "Invalid request body")))).as("application/json"))
    }
  }

  /**
   * Delete a comment
   */
  def delete = jwtAuthentication.async { implicit request =>
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map { json =>
      try {
        val uuid = (json \ "uuid").as[String]

        commentsModel.delete(uuid, request.userInfo.uuid).flatMap {
          result => {
            Future(Ok(Json.stringify(Json.obj(("success", true)))).as("application/json"))
          }
        }.recover {
          case e: Exception => {
            e.printStackTrace()
            InternalServerError(Json.stringify(Json.obj(("success", false), ("error", "An internal server error occurred")))).as("application/json")
          }
        }
      } catch {
        case e @ (_ : play.api.libs.json.JsResultException | _ : java.util.NoSuchElementException) => {
          Future(BadRequest(Json.stringify(Json.obj(("success", false), ("error", "Fields are missing from the JSON body")))).as("application/json"))
        }

        case e:Exception => {
          e.printStackTrace()
          Future(InternalServerError(Json.stringify(Json.obj(("success", false), ("error", "An internal server error occurred")))).as("application/json"))  
        }
      }
    }.getOrElse {
      Future(BadRequest(Json.stringify(Json.obj(("success", false), ("error", "Invalid request body")))).as("application/json"))
    }
  }
}
