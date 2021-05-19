package shop.services

import cats.effect.{ Bracket, BracketThrow, Resource }
import cats.implicits._
import shop.domain.brand.{ Brand, BrandId, BrandName }
import shop.services.BrandSQL.{ insertBrand, selectAll }
import skunk._
import skunk.codec.all._
import skunk.{ Codec, Command, Query }
import skunk.implicits._

import java.util.UUID

trait Brands[F[_]] {
  def findAll: F[List[Brand]]
  def create(name: BrandName): F[Unit]
}

object Brands {
  def make[F[_]: BracketThrow](sessionPool: Resource[F, Session[F]]): F[Brands[F]] =
    Bracket[F, Throwable].pure(new LiveBrands[F](sessionPool))
}

final class LiveBrands[F[_]: BracketThrow](sessionPool: Resource[F, Session[F]]) extends Brands[F] {
  override def findAll: F[List[Brand]] = sessionPool.use(session => session.execute(selectAll))

  override def create(name: BrandName): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertBrand).use(cmd => cmd.execute(name.toBrand(BrandId(UUID.randomUUID()))).void)
    }
}

private object BrandSQL {
  val brandId: Codec[BrandId]     = uuid.imap[BrandId](uuid => BrandId(uuid))(bId => bId.value)
  val brandName: Codec[BrandName] = varchar.imap[BrandName](varchar => BrandName(varchar))(bName => bName.value)
  val brand: Codec[Brand] =
    (brandId ~ brandName).imap[Brand] {
      case bId ~ bName => Brand(bId, bName)
    }(b => b.uuid ~ b.name)
  val selectAll: Query[Void, Brand] =
    sql"""
         SELECT * FROM brands
       """.query(brand)
  val insertBrand: Command[Brand] =
    sql"""
         INSERT INTO brands
         VALUES ($brand)
       """.command
}
