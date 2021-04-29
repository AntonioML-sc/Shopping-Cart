package shop.services

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
  def make[F[_]: Sync]: F[Brands[F]] = Ref.of[F, List[Brand]](List.empty[Brand]).map(ref => new RefBrands[F](ref))
}

final class RefBrands[F[_]](ref: Ref[F, List[Brand]]) extends Brands[F] {
  override def findAll: F[List[Brand]] = ref.get

  override def create(name: BrandName): F[Unit] = ref.update { brands =>
    brands.appended(Brand(BrandId(UUID.randomUUID()), name))
  }
}
