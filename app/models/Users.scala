package models

import java.util.Date
import javax.inject.Inject
 
import play.api.db.DBApi

import anorm._
import anorm.SqlParser._

import scala.concurrent.Future
 
case class Users(
    uuid: String,
    email: String,
    password: String,
    salt: String,
    alg: String,
    verificationHash: String,
    verified: Boolean,
    createdOn: Date,
    updatedOn: Date,
    verifiedOn: Date
)

@javax.inject.Singleton
class UsersModel @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")
  /**
   * Parse a User from a ResultSet
   */
  private val simple = {
    get[String]("users.uuid") ~
      get[String]("users.email") ~
      get[String]("users.password") ~
      get[String]("users.salt") ~
      get[String]("users.alg") ~
      get[String]("users.verificationHash") ~
      get[Boolean]("users.verified") ~
      get[Date]("users.createdOn") ~
      get[Date]("users.updatedOn") ~
      get[Date]("users.verifiedOn") map {
      case uuid ~ email ~ password ~ salt ~ alg ~ verificationHash ~ verified ~ createdOn ~ updatedOn ~ verifiedOn =>
        Users(uuid, email, password, salt, alg, verificationHash, verified, createdOn, updatedOn, verifiedOn)
    }
  }

  // -- Queries

  def findAll(): Seq[Users] = {
    db.withConnection { implicit connection =>
      SQL("select uuid, email from users").as(simple.*)
    }
  }

}