package shop.http.routes

import cats.{ Defer, Monad }
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import shop.domain.brand._
import shop.domain.item.ItemIdParam
import shop.http.json._
import shop.http.params._
import shop.services.Items

final class ItemRoutes[F[_]: Defer: Monad](items: Items[F]) extends Http4sDsl[F] {
  private[routes] val prefixPath: String = "/items"
  object BrandQueryParam extends OptionalQueryParamDecoderMatcher[BrandNameParam]("brandName")
  object ItemIdQueryParam extends QueryParamDecoderMatcher[ItemIdParam]("itemId")
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? BrandQueryParam(brandName) =>
      Ok(brandName.fold(items.findAll)(b => items.findByBrand(b.toDomain)))
    case GET -> Root / "byId" :? ItemIdQueryParam(itemId) =>
      Ok(items.findById(itemId.toDomain))
  }
  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)
}

object ItemRoutes {
  def apply[F[_]: Defer: Monad](items: Items[F]): ItemRoutes[F] = new ItemRoutes[F](items)
}
