package shop.modules

import cats.effect.Sync
import shop.services.{Brands, Categories, Items}
import cats.implicits._

object Services {
  def make[F[_]: Sync]: F[Services[F]] =
    for {
      brands     <- Brands.make[F]
      items      <- Items.make[F]
      categories <- Categories.make[F]
    } yield Services(brands, items, categories)
}

final case class Services[F[_]] private (brands: Brands[F], items: Items[F], categories: Categories[F])
