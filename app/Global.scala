import javax.inject._

import com.google.inject.AbstractModule
import model.{Book, BooksRepo}

import scala.concurrent.ExecutionContext

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[OnStartup]).asEagerSingleton()
  }
}

@Singleton
class OnStartup  @Inject()(booksRepo: BooksRepo)(implicit ec: ExecutionContext) {
    booksRepo.listAll map { list =>
      if (list.isEmpty) {
        val books = Seq(
          Book(Option(1L), "Java 1sf", 50, "Doug Lea"),
          Book(Option(2L), "Java 2nd", 55, "Doug Lea"),
          Book(Option(3L), "Java 3rd", 60, "Doug Lea"),
          Book(Option(4L), "Scala 1st", 100, "Odersky Martin"),
          Book(Option(5L), "Scala 2nd", 110, "Odersky Martin"),
          Book(Option(6L), "Scala 3rd", 120, "Odersky Martin"))
        books.map(booksRepo.add)
      }
    }
}

