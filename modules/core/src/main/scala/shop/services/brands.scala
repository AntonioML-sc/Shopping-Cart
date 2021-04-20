package shop.services

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import shop.domain.brand.{Brand, BrandId, BrandName}

import java.util.UUID

trait Brands[F[_]] {
  def findAll: F[List[Brand]]
}

object Brands {
  def apply[F[_]: Applicative]: Brands[F] = new LiveBrands[F]
  def make[F[_]: Applicative]: F[Brands[F]] = Applicative[F].pure(new LiveBrands[F])
}

final class LiveBrands[F[_]: Applicative] extends Brands[F] {
  // override def findAll: F[List[String]] = List("Jackson", "Gibson", "Fender", "Ibanez", "BCRich").pure[F]

  override def findAll: F[List[Brand]] = List(
    Brand(BrandId(UUID.randomUUID()), BrandName("Jackson")),
    Brand(BrandId(UUID.randomUUID()), BrandName("Gibson")),
    Brand(BrandId(UUID.randomUUID()), BrandName("BCRich")),
    Brand(BrandId(UUID.randomUUID()), BrandName("Fender")),
    Brand(BrandId(UUID.randomUUID()), BrandName("Ibanez"))
  ).pure[F]
}
