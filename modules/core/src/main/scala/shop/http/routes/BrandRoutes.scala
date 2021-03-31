package shop.http.routes

import cats.{Defer, Monad}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class BrandRoutes[F[_] : Defer : Monad] extends Http4sDsl[F] {
  private[routes] val prefixPath: String = "/brands"
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root => Ok("MyBrand")
  }
  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)

}
