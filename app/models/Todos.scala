package models

import java.util.Date
import javax.inject.Inject
 
import play.api.db._

import anorm._
import anorm.SqlParser._

import scala.concurrent.Future
import java.util.UUID

import org.postgresql.util.PSQLException
 
/** A todo 
 *
 *  @constructor create a new todo 
 *  @param uuid [String] the uuid of the todo. New todos will have this overwritten if provided
 *  @param text [String] [Required] the text of the todo
 *  @param checked [Boolean] the checked state of the todo
 *  @param belongsToUser [String] the uuid of the user it belongs to
 *  @param isDeleted [Boolean] the deleted state of the todo
*  @param createdOn [Date] the timestamp the todo was created in the database.
*  @param updatedOn [Date] the timestamp the todo object was last updated on in the database.
 */
case class Todos(
    uuid: String = "",
    text: String,
    checked: Boolean = false,
    belongsToUser: String,
    isDeleted: Boolean = false,
    createdOn: Option[Date] = None,
    updatedOn: Option[Date] = None
)

@javax.inject.Singleton
class TodosModel @Inject()(db: Database)(implicit ec: DatabaseExecutionContext) {
  /**
   * Parse a User from a ResultSet
   */
  private val simple = {
    get[String]("todos.uuid") ~
      get[String]("todos.text") ~
      get[Boolean]("todos.checked") ~
      get[String]("todos.belongsToUser") ~
      get[Boolean]("todos.isDeleted") ~
      get[Option[Date]]("todos.createdOn") ~
      get[Option[Date]]("todos.updatedOn") map {
      
      case uuid ~ text ~ checked ~ belongsToUser ~ isDeleted ~ createdOn ~ updatedOn =>
        Todos(uuid, text, checked, belongsToUser, isDeleted, createdOn, updatedOn)
    }
  }

  /** Create a new todo
  *
  *  @param todo [Required] a todo object representing the new todo to be created.
  */
  def create(todo: Todos): Future[Option[Long]] = Future {
    val uuid = UUID.randomUUID.toString()

    db.withConnection { implicit connection =>
     try {
      SQL("""
        insert into todos (uuid, text, checked, belongsToUser, isDeleted) 
        values ({uuid}, {text}, {checked}, {belongsToUser}, {isDeleted})
      """).on(
        'uuid -> uuid,
        'text -> todo.text,
        'checked -> todo.checked,
        'belongsToUser -> todo.belongsToUser,
        'isDeleted -> todo.isDeleted
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

  /** Find all the todos available in the database
  */
  def findAll(): Future[Seq[Todos]] = Future {
    db.withConnection { implicit connection =>
      SQL("select * from todos").as(simple.*)
    }
  }(ec)

  /** Find all non-deleted todos belonging to the given user uuid
    * @param uuid String the uuid of the user to search bu
  */
    def findByUser(uuid: String): Future[Seq[Todos]] = Future {
      db.withConnection { implicit connection =>
        SQL("select * from todos where belongsToUser = {uuid} and isDeleted is false").on(
          'uuid -> uuid,
        ).as(simple.*)
      }
    }(ec)

  /** Toggle the given todo as completed or not
    * @param uuid String the uuid of the todo to modify
    * @param user String the uuid of the user who created the todo
    * @param completed Boolean if the todo was completed or not
  */
    def toggleCompleted(uuid: String, user: String, completed: Boolean): Future[Int] = Future {
      db.withConnection { implicit connection =>
        SQL("""
         update todos set checked = {completed}, updatedOn = now()
         where uuid = {uuid} and belongsToUser = {user}
        """).on(
          'completed -> completed,
          'uuid -> uuid,
          'user -> user
        ).executeUpdate()
      }
    }(ec)

  /** Mark the given todo as deleted
  * @param uuid String the uuid of the todo to modify
  * @param user String the uuid of the user who created the todo
  */
    def delete(uuid: String, user: String): Future[Int] = Future {
      db.withConnection { implicit connection =>
        SQL("""
         update todos set isDeleted = true, updatedOn = now()
         where uuid = {uuid} and belongsToUser = {user}
        """).on(
          'uuid -> uuid,
          'user -> user
        ).executeUpdate()
      }
    }(ec)

  /** Update the given todo text in the database as long as it is not deleted
  * @param uuid String the uuid of the todo to modify
  * @param user String the uuid of the user who created the todo
  * @param text String the new todo text
  */
    def edit(uuid: String, user: String, text: String): Future[Int] = Future {
      db.withConnection { implicit connection =>
        SQL("""
         update todos set text = {text}, updatedOn = now()
         where uuid = {uuid} and belongsToUser = {user} and isDeleted is false
        """).on(
          'uuid -> uuid,
          'user -> user,
          'text -> text
        ).executeUpdate()
      }
    }(ec)
}