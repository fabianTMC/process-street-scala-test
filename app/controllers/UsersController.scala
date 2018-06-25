package controllers

import javax.inject._

import play.api.mvc._
import models._
import utilities._

import play.api.libs.json._

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller handles all the operations that are made on the Users model
 */
@Singleton
class UsersController @Inject()(cc: ControllerComponents, usersModel: UsersModel) (implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {
    implicit val userFormat = Json.format[Users]

  /**
   * List all the users available in the database
   */
  def index = Action.async { implicit request =>
    usersModel.findAll().map { result =>
      Ok(Json.stringify(Json.toJson(result))).as("application/json")
    }
  }

  /**
   * Create a new user from the JSON body sent
   */
  def create = Action.async { implicit request =>
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map { json =>
      try {
        val email = (json \ "email").as[String]
        val password = (json \ "password").as[String]

        if(Validations.isPasswordAcceptable(password)) {

          usersModel.create(Users(email = email, password = password)).flatMap {
            result => {
              if(result == Some(-1)) {
                Future(Ok(Json.stringify(Json.obj(("success", false), ("error", "This email address is already signed up")))).as("application/json"))
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
        } else {
          Future(Ok(Json.stringify(Json.obj(("success", false), ("error", "Invalid password provided")))).as("application/json"))
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
   * Login a user
   */
  def login = Action.async { implicit request =>
    val body: AnyContent = request.body
    val jsonBody: Option[JsValue] = body.asJson

    jsonBody.map { json =>
      try {
        val email = (json \ "email").as[String]
        val password = (json \ "password").as[String]

        usersModel.findByEmail(email).map { result =>
          if(result.size == 1) {
            if(PasswordHelper.hashPassword(password, result(0).salt).equals(result(0).password)) {
              val token = JWT.createToken(Json.stringify(Json.obj(("uuid", result(0).uuid))).toString())

              Ok(Json.stringify(Json.obj(("success", true), ("token", token)))).as("application/json")
            } else {
              Ok(Json.stringify(Json.obj(("success", false), ("error", "Invalid login credentials")))).as("application/json")
            }
          } else {
            Ok(Json.stringify(Json.obj(("success", false), ("error", "Invalid login credentials")))).as("application/json")
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
