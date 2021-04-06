package shop.http.routes

import cats.{Defer, Monad}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class CategoryRoutes[F[_] : Defer : Monad] extends Http4sDsl[F] {
  private[routes] val prefixPath: String = "/categories"
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root => Ok("MyCategory")
  }
  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)
}

object CategoryRoutes {
  def apply[F[_] : Defer : Monad]: CategoryRoutes[F] = new CategoryRoutes[F]
}
