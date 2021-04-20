package shop.domain

import io.estatico.newtype.macros.newtype

import java.util.UUID

object category {
  @newtype case class CategoryId(value: UUID)

  @newtype case class CategoryName(value: String)

  case class Category(uuid: CategoryId, name: CategoryName)

}


