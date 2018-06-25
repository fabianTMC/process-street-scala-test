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
    createdOn: Option[Date] = None,
    updatedOn: Option[Date] = None,
    verifiedOn: Option[Date] = None
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
      get[Option[Date]]("users.createdOn") ~
      get[Option[Date]]("users.updatedOn") ~
      get[Option[Date]]("users.verifiedOn") map {
      case uuid ~ email ~ password ~ salt ~ alg ~ verificationHash ~ verified ~ createdOn ~ updatedOn ~ verifiedOn =>
        Users(uuid, email, password, salt, alg, verificationHash, verified, createdOn, updatedOn, verifiedOn)
    }
  }

  // -- Queries

  def create(user: Users): Future[Option[Long]] = Future {
    db.withConnection { implicit connection =>
      SQL("""
        insert into users (uuid, email, password, salt, alg, verificationHash, verified) 
        values ({uuid}, {email}, {password}, {salt}, {alg}, {verificationHash}, false)
      """).on(
        'uuid -> user.uuid,
        'email -> user.email,
        'password -> user.password,
        'salt -> user.salt,
        'alg -> user.alg,
        'verificationHash -> user.verificationHash,
      ).executeInsert()
    }
  }(ec)

  def findAll(): Future[Seq[Users]] = Future {
    db.withConnection { implicit connection =>
      SQL("select * from users").as(simple.*)
    }
  }(ec)

}