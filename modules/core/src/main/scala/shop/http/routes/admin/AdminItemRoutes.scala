package shop.http.routes.admin

import cats.implicits._
import cats.{ Defer, Monad, MonadError }
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import shop.domain.item.{ CreateItemParam, ItemId, ItemName, RenameItemParam, UpdatePriceParam }
import shop.http.json.{ createItemParamDecoder, renameItemParamDecoder, updateItemPriceDecoder }
import shop.services.Items

import java.util.UUID

final class AdminItemRoutes[F[_]: Defer: Monad: JsonDecoder](items: Items[F])(implicit ME: MonadError[F, Throwable])
    extends Http4sDsl[F] {
  private[admin] val prefixPath: String = "/items"
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case DELETE -> Root / "all"            => Ok(items.clearAll)
    case DELETE -> Root / "byId" / id      => Ok(items.deleteById(ItemId(UUID.fromString(id))))
    case DELETE -> Root / "byName" / iName => Ok(items.deleteByName(ItemName(iName)))
    case ar @ POST -> Root =>
      ar.asJsonDecode[CreateItemParam]
        .attempt
        .flatMap(ei => ei.fold(e => BadRequest(e.getMessage), cip => Created(items.create(cip.toDomain))))
    case ar @ PUT -> Root / "updatePrice" =>
      ar.asJsonDecode[UpdatePriceParam]
        .flatMap(upp => Ok(items.updatePrice(upp.toDomain)))
    case ar @ PUT -> Root / "rename" =>
      ar.asJsonDecode[RenameItemParam]
        .flatMap(rp => Ok(items.rename(rp.toDomain)))
  }
  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)
}

object AdminItemRoutes {
  def apply[F[_]: Defer: Monad: JsonDecoder](items: Items[F])(
      implicit ME: MonadError[F, Throwable]
  ): AdminItemRoutes[F] = new AdminItemRoutes[F](items)
}
