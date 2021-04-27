package shop.services

import cats.Applicative
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import shop.domain.brand.{Brand, BrandId, BrandName}

import java.util.UUID

trait Brands[F[_]] {
  def findAll: F[List[Brand]]
  def create(name: BrandName): F[Unit]
}

object Brands {
  // def apply[F[_]: Applicative]: Brands[F] = new LiveBrands[F]
  //  def make[F[_]: Applicative]: F[Brands[F]] = Applicative[F].pure(new LiveBrands[F])
  def make[F[_]: Sync]: F[Brands[F]] = Ref.of[F, List[Brand]](List.empty[Brand]).map(ref => new RefBrands[F](ref))
}

//final class LiveBrands[F[_]: Applicative] extends Brands[F] {
  // override def findAll: F[List[String]] = List("Jackson", "Gibson", "Fender", "Ibanez", "BCRich").pure[F]

//  override def findAll: F[List[Brand]] = List(
//    Brand(BrandId(UUID.randomUUID()), BrandName("Jackson")),
//    Brand(BrandId(UUID.randomUUID()), BrandName("Gibson")),
//    Brand(BrandId(UUID.randomUUID()), BrandName("BCRich")),
//    Brand(BrandId(UUID.randomUUID()), BrandName("Fender")),
//    Brand(BrandId(UUID.randomUUID()), BrandName("Ibanez"))
//  ).pure[F]

//  override def create(name: BrandName): F[Unit] = ???
//}

final class RefBrands[F[_]](ref: Ref[F, List[Brand]]) extends Brands[F] {
  override def findAll: F[List[Brand]] = ref.get

  override def create(name: BrandName): F[Unit] = ref.update { brands =>
    brands.appended(Brand(BrandId(UUID.randomUUID()), name))
  }
}
