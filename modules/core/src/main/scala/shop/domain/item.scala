package shop.domain

import io.estatico.newtype.macros.newtype

import java.util.UUID

object item {
  @newtype case class ItemId(value: UUID)

  @newtype case class ItemName(value: String)

  case class Item(uuid: ItemId, name: ItemName)
}


