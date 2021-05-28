package shop.services

import cats.effect.{ Bracket, BracketThrow, Resource }
import cats.implicits._
import shop.domain.brand.{ Brand, BrandId, BrandName, NewBrandName, OldBrandName, RenameBrandInfo }
import shop.effects.GenUUID
import shop.services.BrandSQL.{ deleteAll, deleteBrandById, deleteBrandByName, insertBrand, renameBrand, selectAll }
import skunk._
import skunk.codec.all._
import skunk.data.Completion.{ Delete, Insert, Update }
import skunk.{ Codec, Command, Query }
import skunk.implicits._

trait Brands[F[_]] {
  def findAll: F[List[Brand]]
  def create(name: BrandName): F[Boolean]
  def deleteById(id: BrandId): F[Boolean]
  def deleteByName(name: BrandName): F[Boolean]
  def clearAll: F[Boolean]
  def rename(renameInfo: RenameBrandInfo): F[Boolean]

}

object Brands {
  def make[F[_]: BracketThrow: GenUUID](sessionPool: Resource[F, Session[F]]): F[Brands[F]] =
    Bracket[F, Throwable].pure(new LiveBrands[F](sessionPool))
}

final class LiveBrands[F[_]: BracketThrow: GenUUID](sessionPool: Resource[F, Session[F]]) extends Brands[F] {
  override def findAll: F[List[Brand]] = sessionPool.use(session => session.execute(selectAll))

  override def create(name: BrandName): F[Boolean] =
    sessionPool.use { session =>
      session
        .prepare(insertBrand)
        .use { cmd =>
          GenUUID[F].make.flatMap(uuid => cmd.execute(name.toBrand(BrandId(uuid)))).map {
            case Insert(1) => true
            case _         => false
          }
        }
    }

  override def deleteById(id: BrandId): F[Boolean] =
    sessionPool.use { session =>
      session
        .prepare(deleteBrandById)
        .use { cmd =>
          cmd.execute(id).map {
            case Delete(0) => false
            case Delete(_) => true
            case _         => false
          }
        }
    }

  override def deleteByName(name: BrandName): F[Boolean] =
    sessionPool.use { session =>
      session
        .prepare(deleteBrandByName)
        .use { cmd =>
          cmd.execute(name).map {
            case Delete(0) => false
            case Delete(_) => true
            case _         => false
          }
        }
    }

  override def clearAll: F[Boolean] =
    sessionPool.use { session =>
      session.execute(deleteAll).map {
        case Delete(0) => false
        case Delete(_) => true
        case _         => false
      }
    }

  override def rename(renameInfo: RenameBrandInfo): F[Boolean] =
    sessionPool.use { session =>
      session
        .prepare(renameBrand)
        .use { cmd =>
          cmd.execute(renameInfo).map {
            case Update(1) => true
            case _         => false
          }
        }
    }

}

private object BrandSQL {
  val brandId: Codec[BrandId]     = uuid.imap[BrandId](uuid => BrandId(uuid))(bId => bId.value)
  val brandName: Codec[BrandName] = varchar.imap[BrandName](varchar => BrandName(varchar))(bName => bName.value)
  val newBrandName: Codec[NewBrandName] =
    varchar.imap[NewBrandName](varchar => NewBrandName(varchar))(bName => bName.value)
  val oldBrandName: Codec[OldBrandName] =
    varchar.imap[OldBrandName](varchar => OldBrandName(varchar))(bName => bName.value)
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
  val deleteBrandById: Command[BrandId] =
    sql"""
         DELETE FROM brands
         WHERE uuid = $brandId
       """.command
  val deleteBrandByName: Command[BrandName] =
    sql"""
         DELETE FROM brands
         WHERE name = $brandName
       """.command
  val deleteAll: Command[Void] =
    sql"""
         DELETE FROM brands
       """.command
  val renameBrand: Command[RenameBrandInfo] =
    sql"""
         UPDATE brands
         SET name = $newBrandName
         WHERE name = $oldBrandName
       """.command.contramap { case RenameBrandInfo(newN, oldN) => newN ~ oldN }
}
