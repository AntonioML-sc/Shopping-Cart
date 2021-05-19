package shop.modules

import cats.effect.{ Resource, Sync }
import shop.services.{ Brands, Categories, Items }
import cats.implicits._
import skunk.Session

object Services {
  def make[F[_]: Sync](sessionPool: Resource[F, Session[F]]): F[Services[F]] =
    for {
      brands     <- Brands.make[F](sessionPool)
      items      <- Items.make[F](sessionPool)
      categories <- Categories.make[F](sessionPool)
    } yield Services(brands, items, categories)
}

final case class Services[F[_]] private (brands: Brands[F], items: Items[F], categories: Categories[F])
