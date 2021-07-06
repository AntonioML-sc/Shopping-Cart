package shop.http.routes.admin

import cats.implicits._
import cats.{ Defer, Monad, MonadError }
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import shop.domain.brand.{ BrandId, BrandName, BrandNameParam, OldBrandName, RenameBrandInfo }
import shop.http.json.brandNameParamDecoder
import shop.services.Brands

import java.util.UUID

final class AdminBrandRoutes[F[_]: Defer: Monad: JsonDecoder](brands: Brands[F])(implicit ME: MonadError[F, Throwable])
    extends Http4sDsl[F] {
  private[admin] val prefixPath: String = "/brands"
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case ar @ POST -> Root                 => ar.asJsonDecode[BrandNameParam].flatMap(bp => Created(brands.create(bp.toDomain)))
    case DELETE -> Root / "all"            => Ok(brands.clearAll)
    case DELETE -> Root / "byId" / id      => Ok(brands.deleteById(BrandId(UUID.fromString(id))))
    case DELETE -> Root / "byName" / iName => Ok(brands.deleteByName(BrandName(iName)))
    case newName @ PUT -> Root / oldName =>
      newName
        .asJsonDecode[BrandNameParam]
        .flatMap(nName => Ok(brands.rename(RenameBrandInfo(nName.toNewName, OldBrandName(oldName)))))
  }
  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)
}

object AdminBrandRoutes {
  def apply[F[_]: Defer: Monad: JsonDecoder](brands: Brands[F])(
      implicit ME: MonadError[F, Throwable]
  ): AdminBrandRoutes[F] = new AdminBrandRoutes[F](brands)
}
