package shop.http

import cats.Applicative
import eu.timepit.refined.types.all.NonEmptyString
import io.circe._
import io.circe.generic.semiauto.deriveCodec
import io.estatico.newtype.Coercible
import io.estatico.newtype.ops._
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf
import shop.domain.brand.{ Brand, BrandParam }
import shop.domain.category.{ Category, CategoryParam }
import shop.domain.item.{ Item, ItemParam }
import io.circe.refined._

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

  implicit val brandCodec: Codec[Brand]       = deriveCodec[Brand]
  implicit val categoryCodec: Codec[Category] = deriveCodec[Category]
  implicit val itemCodec: Codec[Item]         = deriveCodec[Item]
  implicit val brandParamDecoder: Decoder[BrandParam] =
    Decoder.forProduct1[BrandParam, NonEmptyString]("name")(BrandParam.apply)
  implicit val categoryParamDecoder: Decoder[CategoryParam] =
    Decoder.forProduct1[CategoryParam, NonEmptyString]("name")(CategoryParam.apply)
  implicit val itemParamDecoder: Decoder[ItemParam] =
    Decoder.forProduct1[ItemParam, NonEmptyString]("name")(ItemParam.apply)
}
