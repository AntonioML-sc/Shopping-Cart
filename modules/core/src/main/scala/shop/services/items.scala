package shop.services

import cats.effect.{ Bracket, BracketThrow, Resource }
import cats.implicits._
import shop.domain.item.{ Item, ItemId, ItemName, NewItemName, OldItemName, RenameItemInfo }
import shop.effects.GenUUID
import shop.services.ItemSQL.{ deleteAll, deleteItemById, deleteItemByName, insertItem, renameItem, selectAll }
import skunk._
import skunk.codec.all._
import skunk.data.Completion.{ Delete, Insert, Update }
import skunk.{ Codec, Command, Query }
import skunk.implicits._

trait Items[F[_]] {
  def findAll: F[List[Item]]
  def create(name: ItemName): F[Boolean]
  def deleteById(id: ItemId): F[Boolean]
  def deleteByName(name: ItemName): F[Boolean]
  def clearAll: F[Boolean]
  def modifyByName(renameInfo: RenameItemInfo): F[Boolean]
}

object Items {
  def make[F[_]: BracketThrow: GenUUID](sessionPool: Resource[F, Session[F]]): F[Items[F]] =
    Bracket[F, Throwable].pure(new LiveItems[F](sessionPool))
}

final class LiveItems[F[_]: BracketThrow: GenUUID](sessionPool: Resource[F, Session[F]]) extends Items[F] {
  override def findAll: F[List[Item]] = sessionPool.use(session => session.execute(selectAll))

  override def create(name: ItemName): F[Boolean] =
    sessionPool.use { session =>
      session
        .prepare(insertItem)
        .use { cmd =>
          GenUUID[F].make.flatMap(uuid => cmd.execute(name.toItem(ItemId(uuid)))).map {
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

  override def modifyByName(renameInfo: RenameItemInfo): F[Boolean] =
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

}

private object ItemSQL {
  val itemId: Codec[ItemId]           = uuid.imap[ItemId](uuid => ItemId(uuid))(iId => iId.value)
  val itemName: Codec[ItemName]       = varchar.imap[ItemName](varchar => ItemName(varchar))(iName => iName.value)
  val newItemName: Codec[NewItemName] = varchar.imap[NewItemName](varchar => NewItemName(varchar))(iName => iName.value)
  val oldItemName: Codec[OldItemName] = varchar.imap[OldItemName](varchar => OldItemName(varchar))(iName => iName.value)
  val item: Codec[Item] =
    (itemId ~ itemName).imap[Item] {
      case iId ~ iName => Item(iId, iName)
    }(i => i.uuid ~ i.name)
  val selectAll: Query[Void, Item] =
    sql"""
         SELECT * FROM items
       """.query(item)
  val insertItem: Command[Item] =
    sql"""
         INSERT INTO items
         VALUES ($item)
       """.command
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
         SET name = $newItemName
         WHERE name = $oldItemName
       """.command.contramap { case RenameItemInfo(newN, oldN) => newN ~ oldN }
}
