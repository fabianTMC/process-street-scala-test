package controllers

import javax.inject._

import play.api.mvc._
import models._
import utilities.Validations

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
                Future(Ok("Duplicate email"))
              } else {
                Future(Ok("All okay"))
              }
            }
          }.recover {
            case e: Exception => {
              e.printStackTrace()
              BadRequest("Nope")
            }
          }
        } else {
          Future(BadRequest("Bad password"))
        }
      } catch {
        case e @ (_ : play.api.libs.json.JsResultException | _ : java.util.NoSuchElementException) => {
          Future(BadRequest("Some fields are missing from the json body"))
        }

        case e:Exception => {
          e.printStackTrace()
          Future(BadRequest("An unknown error occurred"))
        }
      }
    }.getOrElse {
      Future(BadRequest("Expecting application/json request body"))
    }
  }
}
