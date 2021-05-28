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
    def toDomain: ItemName     = ItemName(value.value)
    def toNewName: NewItemName = NewItemName(value.value)
    def toOldName: OldItemName = OldItemName(value.value)
  }

  @newtype case class NewItemName(value: String)
  @newtype case class OldItemName(value: String)

  case class RenameItemInfo(newName: NewItemName, oldName: OldItemName)
}
