package shop.services

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import shop.domain.item.{Item, ItemId, ItemName}

import java.util.UUID

trait Items[F[_]] {
  def findAll: F[List[Item]]
  def create(name: ItemName): F[Unit]
}

object Items {
  def make[F[_]: Sync]: F[Items[F]] = Ref.of[F, List[Item]](List.empty[Item]).map(ref => new RefItems[F](ref))
}

final class RefItems[F[_]](ref: Ref[F, List[Item]]) extends Items[F] {
  override def findAll: F[List[Item]] = ref.get

  override def create(name: ItemName): F[Unit] = ref.update { items =>
    items.appended(Item(ItemId(UUID.randomUUID()), name))
  }
}
