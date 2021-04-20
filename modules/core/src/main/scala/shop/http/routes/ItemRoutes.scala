package shop.http.routes

import cats.{Defer, Monad}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import shop.http.json._
import shop.services.Items

final class ItemRoutes[F[_] : Defer : Monad](items: Items[F]) extends Http4sDsl[F] {
  private[routes] val prefixPath: String = "/items"
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root => Ok(items.findAll)
  }
  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)
}

object ItemRoutes {
  def apply[F[_] : Defer : Monad](items: Items[F]): ItemRoutes[F] = new ItemRoutes[F](items)
}
