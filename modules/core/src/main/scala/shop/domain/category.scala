package shop.domain

import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import java.util.UUID

object category {
  @newtype case class CategoryId(value: UUID)

  @newtype case class CategoryName(value: String) {
    def toCategory(categoryId: CategoryId): Category = Category(categoryId, this)
  }

  case class Category(uuid: CategoryId, name: CategoryName)

  @newtype case class CategoryParam(value: NonEmptyString) {
    def toDomain: CategoryName = CategoryName(value.value.toLowerCase.capitalize)
  }
}
