package shop.services

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._
import shop.domain.category.{Category, CategoryId, CategoryName}

import java.util.UUID

trait Categories[F[_]] {
  def findAll: F[List[Category]]
  def create(name: CategoryName): F[Unit]
}

object Categories {
  def make[F[_]: Sync]: F[Categories[F]] = Ref.of[F, List[Category]](List.empty[Category]).map(ref => new RefCategories[F](ref))
}

final class RefCategories[F[_]](ref: Ref[F, List[Category]]) extends Categories[F] {
  override def findAll: F[List[Category]] = ref.get

  override def create(name: CategoryName): F[Unit] = ref.update { categories =>
    categories.appended(Category(CategoryId(UUID.randomUUID()), name))
  }
}
