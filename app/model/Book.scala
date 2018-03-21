package model

import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

case class Book(id: Option[Long], title: String, price: Long, author: String)


class BooksRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.db
  import dbConfig.profile.api._

  class BookTable(tag: Tag) extends Table[Book](tag, "BOOK") {

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def title = column[String]("TITLE")

    def price = column[Long]("PRICE")

    def author = column[String]("AUTHOR")

    override def * = (id.?, title, price, author) <> (Book.tupled, Book.unapply)

  }

  val books = TableQuery[BookTable]

  def add(book: Book): Future[Long] =
    db.run(books returning books.map(_.id) += book)


  def listAll: Future[List[Book]] = {
    db.run(books.to[List].result)
  }

  /**
    * Count books with a filter
    */
  private def count(filter: String): Future[Int] =
    db.run(books.filter(_.title.toLowerCase like filter.toLowerCase()).length.result)

  def list(page: Int, pageSize: Int, filter: String): Future[Page[Book]] = {
    val offset = pageSize * page
    val query = (for {book <- books if book.title.toLowerCase like filter.toLowerCase} yield book)
      .drop(offset)
      .take(pageSize)
    val totalRows = count(filter)
    val result = db.run(query.result)
    result flatMap (books => totalRows map (rows => Page(books, page, offset, rows)))
  }

  /**
    * Filter book with id
    */
  private def filterQuery(id: Long): Query[BookTable, Book, Seq] =
    books.filter(_.id === id)

  /**
    * Find book by id
    */
  def findById(id: Long): Future[Book] = {
    db.run(filterQuery(id).result.head)
  }

  /**
    * Create a new book
    */
  def insert(book: Book): Future[Int] =
    db.run(books += book)

  /**
    * Update book with id
    */
  def update(id: Long, book: Book): Future[Int] =
    db.run(filterQuery(id).update(book))

  /**
    * Delete book with id
    */
  def delete(id: Long): Future[Int] =
    db.run(filterQuery(id).delete)
}