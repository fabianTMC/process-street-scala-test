import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatest.concurrent.ScalaFutures

import models._

import org.scalatest.time.{Millis, Seconds, Span}

/**
 * Unit tests can run without a full Play application.
 */
class CommentsModelSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures {
  implicit val defaultPatience =
  PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  def commentsModel: CommentsModel = app.injector.instanceOf(classOf[CommentsModel])
  def todosModel: TodosModel = app.injector.instanceOf(classOf[TodosModel])
  def usersModel: UsersModel = app.injector.instanceOf(classOf[UsersModel])

  "CommentsModel" should {

    var todoToManipulate = ""
    var commentToManipulate = ""

    whenReady(usersModel.create(Users(email = "todo-comments@test.com", password = "password"))) { result =>
      whenReady(usersModel.findByEmail("todo-comments@test.com")) { result =>
        val uuid = result(0).uuid

        "should create a new todo with a valid users table reference" in {
          whenReady(todosModel.create(Todos(text = "Hello World", belongsToUser = uuid))) { result =>
            result must not equal(Some(-2))

            whenReady(todosModel.findAll()) { todos =>
              todos.size must equal(1)
              todoToManipulate = todos(0).uuid

              todos(0).uuid must not be empty
              todos(0).text must equal("Hello World")
              todos(0).checked must equal(false)
              todos(0).belongsToUser must equal(uuid)
              todos(0).isDeleted must equal(false)
              todos(0).createdOn must not equal(None)
              todos(0).updatedOn must equal(None)
            }
          }
        }

        "should create a new comment" in {
          whenReady(commentsModel.create(Comments(belongsToTodo = todoToManipulate, belongsToUser = uuid, text = "First Comment"))) { result =>
            result must not equal(Some(-2))

            whenReady(commentsModel.findAll()) { comments =>
              comments.size must equal(1)
              commentToManipulate = comments(0).uuid

              comments(0).uuid must not be empty
              comments(0).text must equal("First Comment")
              comments(0).belongsToTodo must equal(todoToManipulate)
              comments(0).belongsToUser must equal(uuid)
              comments(0).isDeleted must equal(false)
              comments(0).createdOn must not equal(None)
              comments(0).updatedOn must equal(None)
            }
          }
        }

        "should update the text of the given comment" in {
          whenReady(commentsModel.edit(commentToManipulate, uuid, "First Comment Updated")) { result =>
            result must not equal(Some(-2))

            whenReady(commentsModel.findByTodoAndUser(todoToManipulate, uuid)) { comments =>
              comments.size must equal(1)
              comments(0).uuid must not be empty
              comments(0).text must equal("First Comment Updated")
              comments(0).belongsToTodo must equal(todoToManipulate)
              comments(0).belongsToUser must equal(uuid)
              comments(0).isDeleted must equal(false)
              comments(0).createdOn must not equal(None)
              comments(0).updatedOn must not equal(None)
            }
          }
        }

        "should create a new comment from a second user" in {
          whenReady(usersModel.create(Users(email = "todo-comments-1@test.com", password = "password"))) { result =>
            whenReady(usersModel.findByEmail("todo-comments-1@test.com")) { result =>
              val uuid2 = result(0).uuid

              whenReady(todosModel.create(Todos(text = "Hello World", belongsToUser = uuid))) { result =>

                whenReady(commentsModel.create(Comments(belongsToTodo = todoToManipulate, belongsToUser = uuid2, text = "Second Comment"))) { result =>
                  result must not equal(Some(-2))

                  whenReady(commentsModel.findAll()) { comments =>
                    comments.size must equal(2)
                  }

                  whenReady(commentsModel.findByTodoAndUser(todoToManipulate, uuid2)) { comments =>
                    comments.size must equal(1)
                  }
                }
              }
            }
          }
        }

        "should mark the given comment as deleted" in {
          whenReady(commentsModel.delete(commentToManipulate, uuid)) { result =>
            result must not equal(Some(-2))

            whenReady(commentsModel.findByTodo(todoToManipulate)) { todos =>
              todos.size must equal(1)
            }
          }
        }

        "should not update the text of the given comment as it was deleted" in {
          whenReady(commentsModel.edit(commentToManipulate, uuid, "Hello World 4")) { result =>
            result must equal(0)
          }
        }

      }
    }
  }
  
}
