package controllers

import javax.inject._

import play.api.mvc._
import models._
import utilities.JWTAuthentication

import play.api.libs.json._

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller handles all the operations that are made on the Users model
 */
@Singleton
class TodoController @Inject()(cc: ControllerComponents, jwtAuthentication: JWTAuthentication) (implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {
    implicit val userFormat = Json.format[Users]

  /**
   * List all the users available in the database
   */
  def index = jwtAuthentication { implicit request =>
    Ok("Hello")
  }
}
