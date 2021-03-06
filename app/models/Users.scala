package models

import java.util.Date
import javax.inject.Inject
 
import play.api.db._

import anorm._
import anorm.SqlParser._

import scala.concurrent.Future

import utilities.PasswordHelper

import java.util.UUID

import org.postgresql.util.PSQLException
 
/** A user that has signed up to the application. 
 *
 *  @constructor create a new user with an email address and password.
 *  @param email [String] [Required] the user's email address
 *  @param password [String] [Required] the user's chosen password
 *  @param uuid [String] the uuid of the user. New users will have this overwritten if provided
 *  @param salt [String] the salt used with the password hashing for the user. New users will have this overwritten if provided
 *  @param alg [String] the algorithm used for the password hash. New users will have this overwritten if provided
 *  @param verificationHash [String] verification hash used for email verification. New users will have this overwritten if provided
*  @param verified [Boolean] the verification status for the user. New users will have this overwritten if provided
*  @param createdOn [Date] the timestamp the user was created inthe database.
*  @param updatedOn [Date] the timestamp the user object was last updated on in the database.
*  @param verifiedOn [Date] the timestamp the user verified their email on.
 */
case class Users(
    uuid: String = "",
    email: String,
    password: String,
    salt: String = "",
    alg: String = "",
    verificationHash: String = "",
    verified: Boolean = false,
    createdOn: Option[Date] = None,
    updatedOn: Option[Date] = None,
    verifiedOn: Option[Date] = None
)

@javax.inject.Singleton
class UsersModel @Inject()(db: Database)(implicit ec: DatabaseExecutionContext) {
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

/** Create a new user
 *
 *  @param user [Required] a user object representing the new user to be created. The uuid, password, salt, alg and verificationHash fields will get overwritten in the database.
 */
  def create(user: Users): Future[Option[Long]] = Future {
    val salt = PasswordHelper.generateSalt().toString()
    val alg = "SHA-256"
    val password = PasswordHelper.hashPassword(user.password, salt).toString()

    val verificationHash = UUID.randomUUID.toString()
    val uuid = UUID.randomUUID.toString()

    db.withConnection { implicit connection =>
     try {
      SQL("""
        insert into users (uuid, email, password, salt, alg, verificationHash, verified) 
        values ({uuid}, {email}, {password}, {salt}, {alg}, {verificationHash}, false)
      """).on(
        'uuid -> uuid,
        'email -> user.email,
        'password -> password,
        'salt -> salt,
        'alg -> alg,
        'verificationHash -> verificationHash
      ).executeInsert()
     } catch {
        case e: PSQLException => {
          if(e.getSQLState == "23505") {
            Some(-1.toLong)
          } else {
            None
          }
        }

        // This is for the test libary
        case e: org.h2.jdbc.JdbcSQLException => {
          if(e.getSQLState == "23505") {
            Some(-1.toLong)
          } else {
            None
          }
        }

        case e: Exception => {
          None
        }
     }
    }
  }(ec)

  /** Find all the users available in the database
 */
  def findAll(): Future[Seq[Users]] = Future {
    db.withConnection { implicit connection =>
      SQL("select * from users").as(simple.*)
    }
  }(ec)

  /** Find a user by the given email address
  * @param email String the email address to search by
 */
  def findByEmail(email: String): Future[Seq[Users]] = Future {
    db.withConnection { implicit connection =>
      SQL("select * from users WHERE email = {email}").on(
        'email -> email,
      ).as(simple.*)
    }
  }(ec)

}