package shop.modules

import cats.data.Kleisli
import cats.effect.Sync
import cats.implicits._
//import cats.{Defer, Monad, MonadError}
//import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.{Request, Response}
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import shop.http.routes.admin.{AdminBrandRoutes, AdminCategoryRoutes, AdminItemRoutes}
import shop.http.routes.{BrandRoutes, CategoryRoutes, HealthCheckRoutes, ItemRoutes}
//import shop.services.{Brands, Categories, Items}

object HttpApi {
//  private def brandRoutes[F[_]: Sync]: F[HttpRoutes[F]] =
//    Brands.make.map(brands => BrandRoutes[F](brands).routes <+> AdminBrandRoutes[F](brands).routes)
//
//  private def categoryRoutes[F[_]: Sync]: F[HttpRoutes[F]] =
//    Categories.make.map(categories => CategoryRoutes[F](categories).routes <+> AdminCategoryRoutes[F](categories).routes)
//
//  private def itemRoutes[F[_]: Sync]: F[HttpRoutes[F]] =
//    Items.make.map(items => ItemRoutes[F](items).routes <+> AdminItemRoutes[F](items).routes)

  def make[F[_]: Sync](services: Services[F]): F[HttpApi[F]] = HttpApi(services).pure[F]

//  def httpApp[F[_]: Defer: Monad: Sync](implicit mE: MonadError[F, Throwable]): F[HttpApp[F]] =
//    for {
//      b <- brandRoutes[F]
//      c <- categoryRoutes[F]
//      i <- itemRoutes[F]
//    } yield (b <+> c <+> i <+> HealthCheckRoutes[F].routes).orNotFound
}

final case class HttpApi[F[_]: Sync](services: Services[F]) {
  private val brandRoutes = BrandRoutes[F](services.brands).routes
  private val adminBrandRoutes = AdminBrandRoutes[F](services.brands).routes
  private val itemRoutes = ItemRoutes[F](services.items).routes
  private val adminItemRoutes = AdminItemRoutes[F](services.items).routes
  private val categoryRoutes = CategoryRoutes[F](services.categories).routes
  private val adminCategoryRoutes = AdminCategoryRoutes[F](services.categories).routes
  private val healthCheckRoutes = HealthCheckRoutes[F].routes
  val httpApp: Kleisli[F, Request[F], Response[F]] = (brandRoutes <+> adminBrandRoutes <+> itemRoutes <+>
    adminItemRoutes <+> categoryRoutes <+> adminCategoryRoutes <+> healthCheckRoutes).orNotFound
}