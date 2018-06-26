import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.scalatest.concurrent.ScalaFutures

import models._

import org.scalatest.time.{Millis, Seconds, Span}

/**
 * Unit tests can run without a full Play application.
 */
class TodosModelSpec extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures {
  implicit val defaultPatience =
  PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  def todosModel: TodosModel = app.injector.instanceOf(classOf[TodosModel])
  def usersModel: UsersModel = app.injector.instanceOf(classOf[UsersModel])

  "TodosModel" should {

    var todoToManipulate = ""

    "should not create a new todo because of an invalid users table reference" in {
      whenReady(todosModel.create(Todos(text = "Hello World", belongsToUser = "abc"))) { result =>
        result must equal(Some(-2))
      }
    }

    whenReady(usersModel.create(Users(email = "todo@test.com", password = "password"))) { result =>
      whenReady(usersModel.findByEmail("todo@test.com")) { result =>
        val uuid = result(0).uuid

        "should create a new todo with a valid users table reference" in {
          whenReady(todosModel.create(Todos(text = "Hello World", belongsToUser = uuid))) { result =>
            result must not equal(Some(-2))

            whenReady(todosModel.findAll()) { todos =>
              todos.size must equal(1)
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
      }
    }

    whenReady(usersModel.create(Users(email = "todo2@test.com", password = "password2"))) { result =>
      whenReady(usersModel.findByEmail("todo2@test.com")) { result =>
        val uuid = result(0).uuid

        "should create a new todo with a second valid users table reference and count only 1 todo from that user" in {
          whenReady(todosModel.create(Todos(text = "Hello World 2", belongsToUser = uuid))) { result =>
            result must not equal(Some(-2))

            whenReady(todosModel.findByUser(uuid)) { todos =>
              todos.size must equal(1)
              todoToManipulate = todos(0).uuid

              todos(0).uuid must not be empty
              todos(0).text must equal("Hello World 2")
              todos(0).checked must equal(false)
              todos(0).belongsToUser must equal(uuid)
              todos(0).isDeleted must equal(false)
              todos(0).createdOn must not equal(None)
              todos(0).updatedOn must equal(None)
            }
          }
        }
      }
    }

    whenReady(usersModel.findByEmail("todo2@test.com")) { result =>
        val uuid = result(0).uuid

        "should mark the given todo as checked" in {
          whenReady(todosModel.toggleCompleted(todoToManipulate, uuid, true)) { result =>
            result must not equal(Some(-2))

            whenReady(todosModel.findByUser(uuid)) { todos =>
              todos.size must equal(1)
              todos(0).uuid must not be empty
              todos(0).text must equal("Hello World 2")
              todos(0).checked must equal(true)
              todos(0).belongsToUser must equal(uuid)
              todos(0).isDeleted must equal(false)
              todos(0).createdOn must not equal(None)
              todos(0).updatedOn must not equal(None)
            }
          }
        }

        "should mark the given todo as not checked" in {
          whenReady(todosModel.toggleCompleted(todoToManipulate, uuid, false)) { result =>
            result must not equal(Some(-2))

            whenReady(todosModel.findByUser(uuid)) { todos =>
              todos.size must equal(1)
              todos(0).uuid must not be empty
              todos(0).text must equal("Hello World 2")
              todos(0).checked must equal(false)
              todos(0).belongsToUser must equal(uuid)
              todos(0).isDeleted must equal(false)
              todos(0).createdOn must not equal(None)
              todos(0).updatedOn must not equal(None)
            }
          }
        }

        "should update the text of the given todo" in {
          whenReady(todosModel.edit(todoToManipulate, uuid, "Hello World 3")) { result =>
            result must not equal(Some(-2))

            whenReady(todosModel.findByUser(uuid)) { todos =>
              todos.size must equal(1)
              todos(0).uuid must not be empty
              todos(0).text must equal("Hello World 3")
              todos(0).checked must equal(false)
              todos(0).belongsToUser must equal(uuid)
              todos(0).isDeleted must equal(false)
              todos(0).createdOn must not equal(None)
              todos(0).updatedOn must not equal(None)
            }
          }
        }

        "should mark the given todo as deleted" in {
          whenReady(todosModel.delete(todoToManipulate, uuid)) { result =>
            result must not equal(Some(-2))

            whenReady(todosModel.findByUser(uuid)) { todos =>
              todos.size must equal(0)
            }
          }
        }

        "should not update the text of the given todo as it was deleted" in {
          whenReady(todosModel.edit(todoToManipulate, uuid, "Hello World 4")) { result =>
            result must equal(0)
          }
        }
      }
    
  }
  
}
