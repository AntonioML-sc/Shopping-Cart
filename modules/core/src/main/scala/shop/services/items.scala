package shop.services

import cats.effect._
import cats.syntax.all._
import shop.domain.brand.{ Brand, BrandName }
import shop.domain.category.Category
import shop.domain.item._
import shop.effects.GenUUID
import shop.services.ItemSQL._
import shop.services.BrandSQL.{ brandId, brandName }
import shop.services.CategorySQL.{ categoryId, categoryName }
import shop.ext.skunkx._
import skunk._
import skunk.codec.all._
import skunk.data.Completion.{ Delete, Insert, Update }
import skunk.{ Codec, Command, Query }
import skunk.implicits._
import squants.market.USD

trait Items[F[_]] {
  def findAll: F[List[Item]]
  def findByBrand(brandName: BrandName): F[List[Item]]
  def findById(itemId: ItemId): F[Option[Item]]
  def create(item: CreateItem): F[Boolean]
  def deleteById(id: ItemId): F[Boolean]
  def deleteByName(name: ItemName): F[Boolean]
  def clearAll: F[Boolean]
  def rename(renameInfo: RenameItemInfo): F[Boolean]
  def updatePrice(uItem: UpdatePriceInfo): F[Boolean]
}

object Items {
  def make[F[_]: Sync](sessionPool: Resource[F, Session[F]]): F[Items[F]] =
    Sync[F].delay(new LiveItems[F](sessionPool))
}

final class LiveItems[F[_]: Sync: GenUUID](sessionPool: Resource[F, Session[F]]) extends Items[F] {

  override def findAll: F[List[Item]] = sessionPool.use(session => session.execute(selectAll))

  override def findByBrand(brandName: BrandName): F[List[Item]] =
    sessionPool.use(session => session.prepare(selectByBrand).use(ps => ps.stream(brandName, 1024).compile.toList))

  override def findById(itemId: ItemId): F[Option[Item]] =
    sessionPool.use(session => session.prepare(selectById).use(ps => ps.option(itemId)))

  override def create(item: CreateItem): F[Boolean] =
    sessionPool.use { session =>
      session
        .prepare(insertItem)
        .use { cmd =>
          GenUUID[F].make.flatMap(uuid => cmd.execute(NewItemId(ItemId(uuid)) ~ item)).map {
            case Insert(1) => true
            case _         => false
          }
        }
    }
  override def deleteById(id: ItemId): F[Boolean] =
    sessionPool.use { session =>
      session
        .prepare(deleteItemById)
        .use { cmd =>
          cmd.execute(id).map {
            case Delete(0) => false
            case Delete(_) => true
            case _         => false
          }
        }
    }
  override def deleteByName(name: ItemName): F[Boolean] =
    sessionPool.use { session =>
      session
        .prepare(deleteItemByName)
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
  override def rename(renameInfo: RenameItemInfo): F[Boolean] =
    sessionPool.use { session =>
      session
        .prepare(renameItem)
        .use { cmd =>
          cmd.execute(renameInfo).map {
            case Update(1) => true
            case _         => false
          }
        }
    }
  override def updatePrice(uItem: UpdatePriceInfo): F[Boolean] =
    sessionPool.use { session =>
      session
        .prepare(updateItemPrice)
        .use { cmd =>
          cmd.execute(uItem).map {
            case Update(1) => true
            case _         => false
          }
        }
    }
}

private object ItemSQL {
  // ----- DB Codecs ----- \\

  val itemId: Codec[ItemId]     = uuid.imap[ItemId](uuid => ItemId(uuid))(iId => iId.value)
  val itemName: Codec[ItemName] = varchar.imap[ItemName](varchar => ItemName(varchar))(iName => iName.value)
  val itemDescription: Codec[ItemDescription] =
    varchar.imap[ItemDescription](varchar => ItemDescription(varchar))(_.value)
  val item: Codec[Item] =
    (itemId ~ itemName ~ itemDescription ~ numeric ~ brandId ~ brandName ~ categoryId ~ categoryName).imap[Item] {
      case iId ~ iName ~ iDesc ~ iPrice ~ iBrandId ~ iBrandN ~ iCatId ~ iCatN =>
        Item(iId, iName, iDesc, USD(iPrice), Brand(iBrandId, iBrandN), Category(iCatId, iCatN))
    }(i =>
      i.uuid ~ i.name ~ i.description ~ BigDecimal(i.price.value) ~ i.brand.uuid ~ i.brand.name ~ i.category.uuid ~ i.category.name
    )

  // ----- Queries and commands ----- \\

  val selectAll: Query[Void, Item] =
    sql"""
         SELECT i.uuid, i.name, i.description, i.price, b.uuid, b.name, c.uuid, c.name
         FROM items AS i
         INNER JOIN brands AS b ON i.brand_id = b.uuid
         INNER JOIN categories AS c ON i.category_id = c.uuid
       """.query(item)
  val selectByBrand: Query[BrandName, Item] =
    sql"""
         SELECT i.uuid, i.name, i.description, i.price, b.uuid, b.name, c.uuid, c.name
         FROM items AS i
         INNER JOIN brands AS b ON i.brand_id = b.uuid
         INNER JOIN categories AS c ON i.category_id = c.uuid
         WHERE b.name LIKE ${varchar.cimap[BrandName]}
       """.query(item)
  val selectById: Query[ItemId, Item] =
    sql"""
         SELECT i.uuid, i.name, i.description, i.price, b.uuid, b.name, c.uuid, c.name
         FROM items AS i
         INNER JOIN brands AS b ON i.brand_id = b.uuid
         INNER JOIN categories AS c ON i.category_id = c.uuid
         WHERE i.uuid = ${uuid.cimap[ItemId]}
       """.query(item)
  val insertItem: Command[NewItemId ~ CreateItem] =
    sql"""
         INSERT INTO items
         VALUES ($itemId, $itemName, $itemDescription, $numeric, $brandId, $categoryId)
       """.command.contramap {
      case NewItemId(id) ~ CreateItem(name, desc, price, brandId, catId) =>
        id ~ name ~ desc ~ price.amount ~ brandId ~ catId
    }
  val deleteItemById: Command[ItemId] =
    sql"""
         DELETE FROM items
         WHERE uuid = $itemId
       """.command
  val deleteItemByName: Command[ItemName] =
    sql"""
         DELETE FROM items
         WHERE name = $itemName
       """.command
  val deleteAll: Command[Void] =
    sql"""
         DELETE FROM items
       """.command
  val renameItem: Command[RenameItemInfo] =
    sql"""
         UPDATE items
         SET name = $itemName
         WHERE name = $itemName
       """.command.contramap { case RenameItemInfo(oldN, newN) => newN ~ oldN }
  val updateItemPrice: Command[UpdatePriceInfo] =
    sql"""
         UPDATE items
         SET price = $numeric
         WHERE uuid = $itemId
       """.command.contramap { case ui: UpdatePriceInfo => ui.price.amount ~ ui.uuid }

}
