package shop.modules

import cats.data.Kleisli
import cats.effect.Sync
import cats.implicits._
import org.http4s.{ Request, Response }
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import shop.http.routes.admin.{ AdminBrandRoutes, AdminCategoryRoutes, AdminItemRoutes }
import shop.http.routes.{ BrandRoutes, CategoryRoutes, HealthCheckRoutes, ItemRoutes }

object HttpApi {
  def make[F[_]: Sync](services: Services[F]): F[HttpApi[F]] = HttpApi(services).pure[F]
}

final case class HttpApi[F[_]: Sync](services: Services[F]) {
  private val brandRoutes         = BrandRoutes[F](services.brands).routes
  private val adminBrandRoutes    = AdminBrandRoutes[F](services.brands).routes
  private val itemRoutes          = ItemRoutes[F](services.items).routes
  private val adminItemRoutes     = AdminItemRoutes[F](services.items).routes
  private val categoryRoutes      = CategoryRoutes[F](services.categories).routes
  private val adminCategoryRoutes = AdminCategoryRoutes[F](services.categories).routes
  private val healthCheckRoutes   = HealthCheckRoutes[F](services.healthCheck).routes
  val httpApp: Kleisli[F, Request[F], Response[F]] = (brandRoutes <+> adminBrandRoutes <+> itemRoutes <+>
      adminItemRoutes <+> categoryRoutes <+> adminCategoryRoutes <+> healthCheckRoutes).orNotFound
}
