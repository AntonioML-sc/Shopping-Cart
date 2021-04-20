package shop.services

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import shop.domain.category.{Category, CategoryId, CategoryName}

import java.util.UUID

trait Categories[F[_]] {
  def findAll: F[List[Category]]
}

object Categories {
  def apply[F[_]: Applicative]: Categories[F] = new LiveCategories[F]
  def make[F[_]: Applicative]: F[Categories[F]] = Applicative[F].pure(new LiveCategories[F])
}

final class LiveCategories[F[_]: Applicative] extends Categories[F] {
  // override def findAll: F[List[String]] = List("Guitars").pure[F]

  override def findAll: F[List[Category]] = List(
    Category(CategoryId(UUID.randomUUID()), CategoryName("Guitars"))
  ).pure
}
