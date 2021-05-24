package shop.services

import cats.effect.{ Bracket, BracketThrow, Resource }
import cats.implicits._
import shop.domain.item.{ Item, ItemId, ItemName }
import shop.services.ItemSQL.{ deleteAll, deleteItemById, deleteItemByName, insertItem, modifyItemByName, selectAll }
import skunk._
import skunk.codec.all._
import skunk.{ Codec, Command, Query }
import skunk.implicits._

import java.util.UUID

trait Items[F[_]] {
  def findAll: F[List[Item]]
  def create(name: ItemName): F[Unit]
  def deleteById(id: ItemId): F[Unit]
  def deleteByName(name: ItemName): F[Unit]
  def clearAll: F[Unit]
  def modifyByName(oldName: ItemName, newName: ItemName): F[Unit]
}

object Items {
  def make[F[_]: BracketThrow](sessionPool: Resource[F, Session[F]]): F[Items[F]] =
    Bracket[F, Throwable].pure(new LiveItems[F](sessionPool))
}

final class LiveItems[F[_]: BracketThrow](sessionPool: Resource[F, Session[F]]) extends Items[F] {
  override def findAll: F[List[Item]] = sessionPool.use(session => session.execute(selectAll))

  override def create(name: ItemName): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertItem).use(cmd => cmd.execute(name.toItem(ItemId(UUID.randomUUID()))).void)
    }

  override def deleteById(id: ItemId): F[Unit] =
    sessionPool.use(session => session.prepare(deleteItemById).use(cmd => cmd.execute(id).void))

  override def deleteByName(name: ItemName): F[Unit] =
    sessionPool.use(session => session.prepare(deleteItemByName).use(cmd => cmd.execute(name).void))

  override def clearAll: F[Unit] = sessionPool.use(session => session.execute(deleteAll).void)

  override def modifyByName(oldName: ItemName, newName: ItemName): F[Unit] =
    sessionPool.use(session => session.prepare(modifyItemByName).use(cmd => cmd.execute(oldName ~ newName).void))
}

private object ItemSQL {
  val itemId: Codec[ItemId]     = uuid.imap[ItemId](uuid => ItemId(uuid))(iId => iId.value)
  val itemName: Codec[ItemName] = varchar.imap[ItemName](varchar => ItemName(varchar))(iName => iName.value)
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
  val modifyItemByName: Command[ItemName ~ ItemName] =
    sql"""
         UPDATE items
         SET name = $itemName
         WHERE name = $itemName
       """.command
}
