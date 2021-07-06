package shop.http

import cats.Applicative
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.{ Uuid, ValidBigDecimal }
import eu.timepit.refined.types.all.NonEmptyString
import io.circe._
import io.circe.generic.semiauto._
import io.circe.refined._
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import shop.domain.brand._
import shop.domain.category._
import shop.domain.item.{
  CreateItemParam,
  Item,
  ItemDescParam,
  ItemIdParam,
  ItemNameParam,
  ItemPriceParam,
  RenameItemParam,
  UpdatePriceParam
}
import shop.domain.healthCheck.{ AppStatus, PostgresStatus }
import squants.market._

object json extends JsonCodecs {
  implicit def deriveEntityEncoder[F[_]: Applicative, A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]
}

private[http] trait JsonCodecs {

  // ----- Coercible codecs -----

  implicit def coercibleDecoder[A: Coercible[B, *], B: Decoder]: Decoder[A] =
    Decoder[B].map(_.coerce[A])

  implicit def coercibleEncoder[A: Coercible[B, *], B: Encoder]: Encoder[A] =
    Encoder[B].contramap(_.repr.asInstanceOf[B])

  implicit def coercibleKeyDecoder[A: Coercible[B, *], B: KeyDecoder]: KeyDecoder[A] =
    KeyDecoder[B].map(_.coerce[A])

  implicit def coercibleKeyEncoder[A: Coercible[B, *], B: KeyEncoder]: KeyEncoder[A] =
    KeyEncoder[B].contramap[A](_.repr.asInstanceOf[B])

  // ----- Override some decoders -----

  implicit val brandNameParamDecoder: Decoder[BrandNameParam] =
    Decoder.forProduct1[BrandNameParam, NonEmptyString]("name")(BrandNameParam.apply)
  implicit val brandIdParamDecoder: Decoder[BrandIdParam] =
    Decoder.forProduct1[BrandIdParam, String Refined Uuid]("brandId")(BrandIdParam.apply)

  implicit val categoryNameParamDecoder: Decoder[CategoryNameParam] =
    Decoder.forProduct1[CategoryNameParam, NonEmptyString]("name")(CategoryNameParam.apply)
  implicit val categoryIdParamDecoder: Decoder[CategoryIdParam] =
    Decoder.forProduct1[CategoryIdParam, String Refined Uuid]("categoryId")(CategoryIdParam.apply)

  implicit val itemNameParamDecoder: Decoder[ItemNameParam] =
    Decoder.forProduct1[ItemNameParam, NonEmptyString]("name")(ItemNameParam.apply)
  implicit val itemDescParamDecoder: Decoder[ItemDescParam] =
    Decoder.forProduct1[ItemDescParam, NonEmptyString]("description")(ItemDescParam.apply)
  implicit val itemIdParamDecoder: Decoder[ItemIdParam] =
    Decoder.forProduct1[ItemIdParam, String Refined Uuid]("itemId")(ItemIdParam.apply)
  implicit val itemPriceParamDecoder: Decoder[ItemPriceParam] =
    Decoder.forProduct1[ItemPriceParam, String Refined ValidBigDecimal]("price")(ItemPriceParam.apply)

  implicit val createItemParamDecoder: Decoder[CreateItemParam] =
    Decoder
      .forProduct5[
        CreateItemParam,
        NonEmptyString,
        NonEmptyString,
        String Refined ValidBigDecimal,
        String Refined Uuid,
        String Refined Uuid
      ](
        "name",
        "description",
        "price",
        "brandId",
        "categoryId"
      )((n, d, p, b, c) =>
        CreateItemParam(ItemNameParam(n), ItemDescParam(d), ItemPriceParam(p), BrandIdParam(b), CategoryIdParam(c))
      )

  implicit val updateItemPriceDecoder: Decoder[UpdatePriceParam] =
    Decoder
      .forProduct2[
        UpdatePriceParam,
        String Refined Uuid,
        String Refined ValidBigDecimal
      ]("uuid", "price")((i, p) => UpdatePriceParam(ItemIdParam(i), ItemPriceParam(p)))

  implicit val renameItemParamDecoder: Decoder[RenameItemParam] =
    Decoder
      .forProduct2[
        RenameItemParam,
        NonEmptyString,
        NonEmptyString
      ]("oldName", "newName")((oN, nN) => RenameItemParam(ItemNameParam(oN), ItemNameParam(nN)))

  implicit val postgresStatusDecoder: Decoder[PostgresStatus] =
    Decoder.forProduct1[PostgresStatus, Boolean]("name")(PostgresStatus.apply)

  // ----- Domain codecs -----

  implicit val moneyDecoder: Decoder[Money] =
    Decoder[BigDecimal].map(USD.apply)
  implicit val moneyEncoder: Encoder[Money] =
    Encoder[BigDecimal].contramap(_.amount)

  implicit val brandEncoder: Encoder[Brand]       = deriveEncoder[Brand]
  implicit val categoryEncoder: Encoder[Category] = deriveEncoder[Category]
  implicit val itemEncoder: Encoder[Item]         = deriveEncoder[Item]
  implicit val healthCheckCodec: Codec[AppStatus] = deriveCodec[AppStatus]

}
