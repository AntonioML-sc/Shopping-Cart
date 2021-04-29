package shop.modules

import cats.effect.Sync
import cats.implicits._
import cats.{Defer, Monad, MonadError}
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import shop.http.routes.admin.{AdminBrandRoutes, AdminCategoryRoutes, AdminItemRoutes}
import shop.http.routes.{BrandRoutes, CategoryRoutes, HealthCheckRoutes, ItemRoutes}
import shop.services.{Brands, Categories, Items}

object HttpApi {
  private def brandRoutes[F[_]: Sync]: F[HttpRoutes[F]] =
    Brands.make.map(brands => BrandRoutes[F](brands).routes <+> AdminBrandRoutes[F](brands).routes)

  private def categoryRoutes[F[_]: Sync]: F[HttpRoutes[F]] =
    Categories.make.map(categories => CategoryRoutes[F](categories).routes <+> AdminCategoryRoutes[F](categories).routes)

  private def itemRoutes[F[_]: Sync]: F[HttpRoutes[F]] =
    Items.make.map(items => ItemRoutes[F](items).routes <+> AdminItemRoutes[F](items).routes)

  def httpApp[F[_]: Defer: Monad: Sync](implicit mE: MonadError[F, Throwable]): F[HttpApp[F]] =
    for {
      b <- brandRoutes[F]
      c <- categoryRoutes[F]
      i <- itemRoutes[F]
    } yield (b <+> c <+> i <+> HealthCheckRoutes[F].routes).orNotFound

}
