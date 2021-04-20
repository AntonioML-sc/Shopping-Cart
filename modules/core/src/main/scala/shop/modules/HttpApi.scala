package shop.modules

import cats.implicits.toSemigroupKOps
import cats.{Defer, Monad}
import org.http4s.HttpApp
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import shop.http.routes.{BrandRoutes, CategoryRoutes, HealthCheckRoutes, ItemRoutes}
import shop.services.{Brands, Categories, Items}

object HttpApi {
  def httpApp[F[_]: Defer: Monad]: HttpApp[F] = (BrandRoutes[F](Brands[F]).routes <+>
    CategoryRoutes[F](Categories[F]).routes <+> ItemRoutes[F](Items[F]).routes <+> HealthCheckRoutes[F].routes).orNotFound
}
