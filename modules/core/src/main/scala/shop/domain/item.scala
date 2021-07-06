package shop.domain

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.{ Uuid, ValidBigDecimal }
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import shop.domain.brand.{ Brand, BrandId, BrandIdParam }
import shop.domain.category.{ Category, CategoryId, CategoryIdParam }

import java.util.UUID
import squants.market.{ Money, USD }

object item {
  // ----- Item ----- \\

  @newtype case class ItemId(value: UUID)
  @newtype case class ItemName(value: String)
  @newtype case class ItemDescription(value: String)

  case class Item(
      uuid: ItemId,
      name: ItemName,
      description: ItemDescription,
      price: Money,
      brand: Brand,
      category: Category
  )

  // ----- Create item ----- \\

  @newtype case class ItemNameParam(value: NonEmptyString) {
    def toDomain: ItemName = ItemName(value.value)
  }
  @newtype case class ItemDescParam(value: NonEmptyString) {
    def toDomain: ItemDescription = ItemDescription(value.value)
  }
  @newtype case class ItemPriceParam(value: String Refined ValidBigDecimal) {
    def toUSD: Money = USD(BigDecimal(value.value))
  }
  @newtype case class ItemIdParam(value: String Refined Uuid) {
    def toDomain: ItemId = ItemId(UUID.fromString(value.value))
  }
  case class CreateItem(
      name: ItemName,
      description: ItemDescription,
      price: Money,
      brandId: BrandId,
      categoryId: CategoryId
  )
  case class CreateItemParam(
      name: ItemNameParam,
      description: ItemDescParam,
      price: ItemPriceParam,
      brandId: BrandIdParam,
      categoryId: CategoryIdParam
  ) {
    def toDomain: CreateItem = CreateItem(
      name.toDomain,
      description.toDomain,
      price.toUSD,
      brandId.toDomain,
      categoryId.toDomain
    )
  }
  case class NewItemId(value: ItemId)

  // ----- Update item price ----- \\

  case class UpdatePriceInfo(uuid: ItemId, price: Money)
  case class UpdatePriceParam(uuid: ItemIdParam, price: ItemPriceParam) {
    def toDomain: UpdatePriceInfo = UpdatePriceInfo(
      uuid.toDomain,
      price.toUSD
    )
  }

  // ----- Rename item ----- \\

  case class RenameItemInfo(oldName: ItemName, newName: ItemName)
  case class RenameItemParam(oldName: ItemNameParam, newName: ItemNameParam) {
    def toDomain: RenameItemInfo = RenameItemInfo(
      oldName.toDomain,
      newName.toDomain
    )
  }

}
