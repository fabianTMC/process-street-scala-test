package models

import java.util.Date
import javax.inject.Inject
 
import play.api.db._

import anorm._
import anorm.SqlParser._

import scala.concurrent.Future
import java.util.UUID

import org.postgresql.util.PSQLException
 
/** A comment 
 *
 *  @constructor create a new comment 
 *  @param uuid [String] the uuid of the comment. New comments will have this overwritten if provided
 *  @param text [String] [Required] the text of the comment
 *  @param belongsToTodo [String] the uuid of the too it belongs to
 *  @param belongsToUser [String] the uuid of the user it belongs to
 *  @param isDeleted [Boolean] the deleted state of the comment
*  @param createdOn [Date] the timestamp the comment was created in the database.
*  @param updatedOn [Date] the timestamp the comment object was last updated on in the database.
 */
case class Comments(
    uuid: String = "",
    text: String,
    belongsToTodo: String,
    belongsToUser: String,
    isDeleted: Boolean = false,
    createdOn: Option[Date] = None,
    updatedOn: Option[Date] = None
)

@javax.inject.Singleton
class CommentsModel @Inject()(db: Database)(implicit ec: DatabaseExecutionContext) {
  /**
   * Parse a comment from a ResultSet
   */
  private val simple = {
    get[String]("comments.uuid") ~
      get[String]("comments.text") ~
      get[String]("comments.belongsToTodo") ~
      get[String]("comments.belongsToUser") ~
      get[Boolean]("comments.isDeleted") ~
      get[Option[Date]]("comments.createdOn") ~
      get[Option[Date]]("comments.updatedOn") map {
      
      case uuid ~ text ~ belongsToTodo ~ belongsToUser ~ isDeleted ~ createdOn ~ updatedOn =>
        Comments(uuid, text, belongsToTodo, belongsToUser, isDeleted, createdOn, updatedOn)
    }
  }

  /** Create a new comment
  *
  *  @param comment [Required] a comment object representing the new comment to be created.
  */
  def create(comment: Comments): Future[Option[Long]] = Future {
    val uuid = UUID.randomUUID.toString()

    db.withConnection { implicit connection =>
     try {
      SQL("""
        insert into comments (uuid, text, belongsToTodo, belongsToUser, isDeleted) 
        values ({uuid}, {text}, {belongsToTodo}, {belongsToUser}, {isDeleted})
      """).on(
        'uuid -> uuid,
        'text -> comment.text,
        'belongsToTodo -> comment.belongsToTodo,
        'belongsToUser -> comment.belongsToUser,
        'isDeleted -> comment.isDeleted
      ).executeInsert()
     } catch {
       case e: PSQLException => {
        if(e.getSQLState == "23506") {
            Some(-2.toLong)
          } else {
            None
          }
        }

        // This is for the test libary
        case e: org.h2.jdbc.JdbcSQLException => {
          if(e.getSQLState == "23506") {
            Some(-2.toLong)
          } else {
            None
          }
        }
        
        case e: Exception => {
          e.printStackTrace()
          None
        }
     }
    }
  }(ec)

  /** Find all the comments available in the database
  */
  def findAll(): Future[Seq[Comments]] = Future {
    db.withConnection { implicit connection =>
      SQL("select * from comments").as(simple.*)
    }
  }(ec)

  /** Find all non-deleted todos belonging to the given todo
    * @param uuid String the uuid of the todo to search bu
  */
  def findByTodo(uuid: String): Future[Seq[Comments]] = Future {
    db.withConnection { implicit connection =>
      SQL("select * from comments where belongsToTodo = {uuid} and isDeleted is false").on(
        'uuid -> uuid,
      ).as(simple.*)
    }
  }(ec)

  /** Find all non-deleted todos belonging to the given todo and given user
    * @param uuid String the uuid of the todo to search by
    * @param user String the uuid of the user to search by
  */
  def findByTodoAndUser(uuid: String, user: String): Future[Seq[Comments]] = Future {
    db.withConnection { implicit connection =>
      SQL("select * from comments where belongsToTodo = {uuid} and belongsToUser = {user} and isDeleted is false").on(
        'uuid -> uuid,
        'user -> user,
      ).as(simple.*)
    }
  }(ec)

  /** Mark the given comment as deleted
  * @param uuid String the uuid of the comment to modify
  * @param user String the uuid of the user who created the comment
  */
    def delete(uuid: String, user: String): Future[Int] = Future {
      db.withConnection { implicit connection =>
        SQL("""
         update comments set isDeleted = true, updatedOn = now()
         where uuid = {uuid} and belongsToUser = {user}
        """).on(
          'uuid -> uuid,
          'user -> user
        ).executeUpdate()
      }
    }(ec)

  /** Update the given comment text in the database as long as it is not deleted
  * @param uuid String the uuid of the comment to modify
  * @param user String the uuid of the user who created the comment
  * @param text String the new comment text
  */
    def edit(uuid: String, user: String, text: String): Future[Int] = Future {
      db.withConnection { implicit connection =>
        SQL("""
         update comments set text = {text}, updatedOn = now()
         where uuid = {uuid} and belongsToUser = {user} and isDeleted is false
        """).on(
          'uuid -> uuid,
          'user -> user,
          'text -> text
        ).executeUpdate()
      }
    }(ec)
}