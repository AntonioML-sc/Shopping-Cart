package shop.http.routes.admin

import cats.implicits._
import cats.{ Defer, Monad, MonadError }
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import shop.domain.item.ItemParam
import shop.http.json.itemParamDecoder
import shop.services.Items

final class AdminItemRoutes[F[_]: Defer: Monad: JsonDecoder](items: Items[F])(implicit ME: MonadError[F, Throwable])
    extends Http4sDsl[F] {
  private[admin] val prefixPath: String = "/items"
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case ar @ POST -> Root => ar.asJsonDecode[ItemParam].flatMap(ip => Created(items.create(ip.toDomain)))
  }
  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)
}

object AdminItemRoutes {
  def apply[F[_]: Defer: Monad: JsonDecoder](items: Items[F])(
      implicit ME: MonadError[F, Throwable]
  ): AdminItemRoutes[F] = new AdminItemRoutes[F](items)
}
