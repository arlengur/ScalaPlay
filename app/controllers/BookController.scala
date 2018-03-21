package controllers

import java.util.concurrent.TimeoutException
import javax.inject.Inject

import model.{Book, BooksRepo}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{Action, _}
import views.html

import scala.concurrent.{ExecutionContext, Future}

class BookController @Inject()(cc: MessagesControllerComponents, booksRepo: BooksRepo)
                              (implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {


  /**
    * This result directly redirect to the application home.
    */
  val Home: Result = Redirect(routes.BookController.list())

  /**
    * Handle default path requests, redirect to books list
    */
  def index = Action { implicit request =>
    Home
  }

  /**
    * Display the paginated list of books.
    */
  def list(page: Int, orderBy: Int, filter: String): Action[AnyContent] = Action.async { implicit request =>
    booksRepo.list(page, 5, "%" + filter + "%").map { pageEmp =>
      Ok(html.list(pageEmp, orderBy, filter))
    }.recover {
      case ex: TimeoutException =>
        Logger.error("Problem found in book list process")
        InternalServerError(ex.getMessage)
    }
  }

  /**
    * Display the 'edit form' of a existing book.
    */
  def edit(id: Long): Action[AnyContent] = Action.async { implicit request =>
    booksRepo.findById(id).map(book => Ok(html.edit(id, bookForm.fill(book)))).recover {
      case ex: TimeoutException =>
        Logger.error("Problem found in book edit process")
        InternalServerError(ex.getMessage)
    }
  }

  /**
    * Handle the 'edit form' submission
    */
  def update(id: Long): Action[AnyContent] = Action.async { implicit request =>
    bookForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(html.edit(id, formWithErrors))),
      book => {
        val futureBookUpdate = booksRepo.update(id, book.copy(id = Some(id)))
        futureBookUpdate.map { result =>
          Home.flashing("success" -> "Book %s has been updated".format(book.title))
        }.recover {
          case ex: TimeoutException =>
            Logger.error("Problem found in book update process")
            InternalServerError(ex.getMessage)
        }
      })
  }

  /**
    * Display the 'new book form'.
    */
  def create: Action[AnyContent] = Action { implicit request =>
    Ok(html.create(bookForm))
  }

  /**
    * Handle the 'new book form' submission.
    */
  def save: Action[AnyContent] = Action.async { implicit request =>
    bookForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(html.create(formWithErrors))),
      book => {
        val futureBookInsert = booksRepo.insert(book)
        futureBookInsert.map { result => Home.flashing("success" -> "Book %s has been created".format(book.title)) }.recover {
          case ex: TimeoutException =>
            Logger.error("Problem found in book save process")
            InternalServerError(ex.getMessage)
        }
      })
  }

  /**
    * Handle book deletion
    */
  def delete(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val futureBookDel = booksRepo.delete(id)
    futureBookDel.map { result => Home.flashing("success" -> "Book has been deleted") }.recover {
      case ex: TimeoutException =>
        Logger.error("Problem found in book delete process")
        InternalServerError(ex.getMessage)
    }
  }

  /**
    * Describe the book form (used in both edit and create screens).
    */
  val bookForm = Form(
    mapping(
      "id" -> optional(longNumber),
      "title" -> nonEmptyText,
      "price" -> longNumber,
      "author" -> text)(Book.apply)(Book.unapply))

  def getBooksJson = Action.async { implicit rs =>
    implicit val bookFormat = Json.format[Book]
    booksRepo.listAll map { books =>
      Ok(Json.toJson(books))
    }
  }
}
