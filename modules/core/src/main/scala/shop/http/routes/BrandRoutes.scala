package shop.http.routes

import cats.{Defer, Monad}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import shop.services.Brands
import shop.http.json._

final class BrandRoutes[F[_] : Defer : Monad](brands: Brands[F]) extends Http4sDsl[F] {
  private[routes] val prefixPath: String = "/brands"
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root => Ok(brands.findAll)
  }
  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)

}

object BrandRoutes {
  def apply[F[_] : Defer : Monad](brands: Brands[F]): BrandRoutes[F] = new BrandRoutes[F](brands)
}
