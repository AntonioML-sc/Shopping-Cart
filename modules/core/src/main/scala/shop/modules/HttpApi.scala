package shop.modules

import cats.implicits.toSemigroupKOps
import cats.{Defer, Monad}
import org.http4s.HttpApp
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import shop.http.routes.{BrandRoutes, CategoryRoutes, HealthCheckRoutes, ItemRoutes}

object HttpApi {
  def httpApp[F[_]: Defer: Monad]: HttpApp[F] = (new BrandRoutes[F].routes <+> CategoryRoutes[F].routes <+>
    ItemRoutes[F].routes <+> new HealthCheckRoutes[F].routes).orNotFound
}
