package controllers

import javax.inject.Inject

import model.{Book, BookForm, BooksRepo}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class BookController @Inject()(cc: MessagesControllerComponents, booksRepo: BooksRepo)
                              (implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  def index = Action { implicit request =>
    Ok(views.html.index(bookForm, Seq.empty[Book]))
  }

  val bookForm: Form[BookForm] = Form {
    mapping(
      "title" -> text,
      "price" -> longNumber,
      "author" -> text
    )(BookForm.apply)(BookForm.unapply)
  }

  def addBook = Action.async { implicit request =>
    bookForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.index(bookForm, Seq.empty[Book])))
      },
      data1 => {
        val book = Book(0, data1.title, data1.price, data1.author)
        booksRepo.add(book).map { _ =>
          Redirect(routes.BookController.index()).flashing("success" -> "book.created")
        }
      }
    )
  }

  def getBook(title: String) = Action.async { implicit request =>
    for {
      Some(book) <- booksRepo.findByTitle(title)
    } yield Ok(views.html.show(BookForm(book.title, book.price, book.author)))
  }

  def getBooks = Action.async { implicit rs =>
    implicit val bookFormat = Json.format[Book]
    booksRepo.listAll map { books =>
      Ok(Json.toJson(books))
    }
  }
}
