package model

import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

case class Book(id: Long, title: String, price: Long, author: String)
case class BookForm(title: String, price: Long, author: String)

class BooksRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) (implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.db
  import dbConfig.profile.api._

  class BookTable(tag: Tag) extends Table[Book](tag, "BOOK") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def title = column[String]("TITLE")

    def price = column[Long]("PRICE")

    def author = column[String]("AUTHOR")

    override def * = (id, title, price, author) <> (Book.tupled, Book.unapply)

  }

  val books = TableQuery[BookTable]


  def add(book: Book): Future[Long] =
    db.run(books returning books.map(_.id) += book)


  def findByTitle(title: String): Future[Option[Book]] = {
    db.run(books.filter(_.title === title).result.headOption)
  }

  def listAll: Future[List[Book]] = {
    db.run(books.to[List].result)
  }
}