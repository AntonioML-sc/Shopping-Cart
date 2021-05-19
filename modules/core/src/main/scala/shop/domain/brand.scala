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
    def toDomain: BrandName = BrandName(value.value.toLowerCase.capitalize)
  }
}


