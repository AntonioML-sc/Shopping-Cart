package shop.domain

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Uuid
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype

import java.util.UUID

object category {
  @newtype case class CategoryId(value: UUID)

  @newtype case class CategoryName(value: String) {
    def toCategory(categoryId: CategoryId): Category = Category(categoryId, this)
  }

  case class Category(uuid: CategoryId, name: CategoryName)

  @newtype case class CategoryNameParam(value: NonEmptyString) {
    def toDomain: CategoryName     = CategoryName(value.value.toLowerCase.capitalize)
    def toNewName: NewCategoryName = NewCategoryName(value.value.toLowerCase.capitalize)
    def toOldName: OldCategoryName = OldCategoryName(value.value.toLowerCase.capitalize)
  }

  @newtype case class CategoryIdParam(value: String Refined Uuid) {
    def toDomain: CategoryId = CategoryId(UUID.fromString(value.value))
  }

  @newtype case class NewCategoryName(value: String)
  @newtype case class OldCategoryName(value: String)

  case class RenameCatInfo(newName: NewCategoryName, oldName: OldCategoryName)
}
