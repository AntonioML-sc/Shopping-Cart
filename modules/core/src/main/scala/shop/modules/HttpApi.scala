package shop.modules

import cats.effect.Sync
import cats.implicits._
import cats.{Defer, Monad, MonadError}
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import shop.http.routes.admin.AdminBrandRoutes
import shop.http.routes.{BrandRoutes, CategoryRoutes, HealthCheckRoutes, ItemRoutes}
import shop.services.{Brands, Categories, Items}

object HttpApi {
  private def brandRoutes[F[_] : Sync]: F[HttpRoutes[F]] =
    Brands.make.map(brands => BrandRoutes[F](brands).routes <+> new AdminBrandRoutes[F](brands).routes)

  def httpApp[F[_]: Defer: Monad: Sync](implicit mE: MonadError[F, Throwable]): F[HttpApp[F]] =
    brandRoutes[F].map(br => (br <+>
      CategoryRoutes[F](Categories[F]).routes <+>
      ItemRoutes[F](Items[F]).routes <+>
      HealthCheckRoutes[F].routes).orNotFound)
}
