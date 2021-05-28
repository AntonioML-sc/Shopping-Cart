package shop.domain

import eu.timepit.refined.types.all.NonEmptyString
import java.util.UUID
import io.estatico.newtype.macros.newtype

object brand {
  @newtype case class BrandId(value: UUID)

  @newtype case class BrandName(value: String) {
    def toBrand(brandId: BrandId): Brand = Brand(brandId, this)
  }

  case class Brand(uuid: BrandId, name: BrandName)

  @newtype case class BrandParam(value: NonEmptyString) {
    def toDomain: BrandName     = BrandName(value.value)
    def toNewName: NewBrandName = NewBrandName(value.value)
    def toOldName: OldBrandName = OldBrandName(value.value)
  }

  @newtype case class NewBrandName(value: String)
  @newtype case class OldBrandName(value: String)

  case class RenameBrandInfo(newName: NewBrandName, oldName: OldBrandName)
}
