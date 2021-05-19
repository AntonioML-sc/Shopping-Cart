package shop.http.routes.admin

import cats.implicits._
import cats.{ Defer, Monad, MonadError }
import org.http4s.HttpRoutes
import org.http4s.circe.{ toMessageSynax, JsonDecoder }
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import shop.domain.category.CategoryParam
import shop.http.json.categoryParamDecoder
import shop.services.Categories

final class AdminCategoryRoutes[F[_]: Defer: Monad: JsonDecoder](categories: Categories[F])(
    implicit ME: MonadError[F, Throwable]
) extends Http4sDsl[F] {
  private[admin] val prefixPath: String = "/categories"
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case ar @ POST -> Root => ar.asJsonDecode[CategoryParam].flatMap(cp => Created(categories.create(cp.toDomain)))
  }
  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)
}

object AdminCategoryRoutes {
  def apply[F[_]: Defer: Monad: JsonDecoder](
      categories: Categories[F]
  )(implicit ME: MonadError[F, Throwable]): AdminCategoryRoutes[F] =
    new AdminCategoryRoutes[F](categories)
}
