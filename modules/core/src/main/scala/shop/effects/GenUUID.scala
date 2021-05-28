package shop.effects

import cats.effect.Sync

import java.util.UUID

trait GenUUID[F[_]] {
  def make: F[UUID]
  def fromString(s: String): F[UUID]
}

object GenUUID {
  def apply[F[_]](implicit F: GenUUID[F]): GenUUID[F] = F

  implicit def impl[F[_]: Sync]: GenUUID[F] = new GenUUID[F] {
    override def make: F[UUID] = Sync[F].delay(UUID.randomUUID())

    override def fromString(s: String): F[UUID] = Sync[F].delay(UUID.fromString(s))
  }
}
