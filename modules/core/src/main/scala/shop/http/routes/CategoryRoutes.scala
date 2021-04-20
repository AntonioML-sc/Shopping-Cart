package shop.http.routes

import cats.{Defer, Monad}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import shop.services.Categories
import shop.http.json._

final class CategoryRoutes[F[_] : Defer : Monad](categories: Categories[F]) extends Http4sDsl[F] {
  private[routes] val prefixPath: String = "/categories"
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root => Ok(categories.findAll)
  }
  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)
}

object CategoryRoutes {
  def apply[F[_] : Defer : Monad](categories: Categories[F]): CategoryRoutes[F] = new CategoryRoutes[F](categories)
}
