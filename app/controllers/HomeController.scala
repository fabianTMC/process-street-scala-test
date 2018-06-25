package controllers

import javax.inject._

import play.api.mvc._
import models._

import play.api.libs.json.Json._

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, usersModel: UsersModel) (implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {
    implicit val userFormat = format[Users]

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action.async { implicit request =>
      usersModel.findAll().map { result =>
        Ok(stringify(toJson(result))).as("application/json")
      }
    }
}
