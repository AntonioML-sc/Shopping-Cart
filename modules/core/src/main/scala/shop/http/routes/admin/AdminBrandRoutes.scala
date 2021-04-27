package shop.http.routes.admin

import cats.implicits._
import cats.{Defer, Monad, MonadError}
import org.http4s.HttpRoutes
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import shop.domain.brand.BrandParam
import shop.http.json.brandParamDecoder
import shop.services.Brands

final class AdminBrandRoutes[F[_]: Defer: Monad: JsonDecoder](brands: Brands[F])(implicit ME: MonadError[F, Throwable]) extends Http4sDsl[F] {
  private[admin] val prefixPath: String = "/brands"
   private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
     case ar @ POST -> Root => ar.asJsonDecode[BrandParam].flatMap { bp =>
       Created(brands.create(bp.toDomain))
     }
   }

  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)

}
