package shop.domain

import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import java.util.UUID

object item {
  @newtype case class ItemId(value: UUID)

  @newtype case class ItemName(value: String) {
    def toItem(itemId: ItemId): Item = Item(itemId, this)
  }

  case class Item(uuid: ItemId, name: ItemName)

  @newtype case class ItemParam(value: NonEmptyString) {
    def toDomain: ItemName = ItemName(value.value.toLowerCase.capitalize)
  }
}
