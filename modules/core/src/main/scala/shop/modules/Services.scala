package shop.modules

import cats.effect.{ Concurrent, Resource, Timer }
import shop.services.{ Brands, Categories, HealthCheck, Items }
import cats.implicits._
import skunk.Session

object Services {
  def make[F[_]: Concurrent: Timer](sessionPool: Resource[F, Session[F]]): F[Services[F]] =
    for {
      brands      <- Brands.make[F](sessionPool)
      items       <- Items.make[F](sessionPool)
      categories  <- Categories.make[F](sessionPool)
      healthCheck <- HealthCheck.make[F](sessionPool)
    } yield Services(brands, items, categories, healthCheck)
}

final case class Services[F[_]] private (
    brands: Brands[F],
    items: Items[F],
    categories: Categories[F],
    healthCheck: HealthCheck[F]
)
