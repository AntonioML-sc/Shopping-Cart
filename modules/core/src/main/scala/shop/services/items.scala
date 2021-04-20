package shop.services

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import shop.domain.item.{Item, ItemId, ItemName}

import java.util.UUID

trait Items[F[_]] {
  def findAll: F[List[Item]]
}

object Items {
  def apply[F[_]: Applicative]: Items[F] = new LiveItems[F]
  def make[F[_]: Applicative]: F[Items[F]] = Applicative[F].pure(new LiveItems[F])
}

final class LiveItems[F[_]: Applicative] extends Items[F] {
  // override def findAll: F[List[String]] = List("Standard Stratocaster", "Squier Deluxe Stratocaster",
  // "Deluxe Telecaster", "Fender Jazzmaster", "Les Paul Classic", "SG Standard", "Ibanez GRX20", "X Series Soloist",
  // "X Series V").pure[F]

  override def findAll: F[List[Item]] = List(
    Item(ItemId(UUID.randomUUID()), ItemName("Standard Stratocaster")),
    Item(ItemId(UUID.randomUUID()), ItemName("Squier Deluxe Stratocaster")),
    Item(ItemId(UUID.randomUUID()), ItemName("Deluxe Telecaster")),
    Item(ItemId(UUID.randomUUID()), ItemName("Fender Jazzmaster")),
    Item(ItemId(UUID.randomUUID()), ItemName("Les Paul Classic")),
    Item(ItemId(UUID.randomUUID()), ItemName("SG Standard")),
    Item(ItemId(UUID.randomUUID()), ItemName("Ibanez GRX20")),
    Item(ItemId(UUID.randomUUID()), ItemName("X Series Soloist"))
  ).pure
}
