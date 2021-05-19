package shop.services

import cats.effect.{ Bracket, BracketThrow, Resource }
import cats.implicits._
import shop.domain.item.{ Item, ItemId, ItemName }
import shop.services.ItemSQL.{ insertItem, selectAll }
import skunk._
import skunk.codec.all._
import skunk.{ Codec, Command, Query }
import skunk.implicits._

import java.util.UUID

trait Items[F[_]] {
  def findAll: F[List[Item]]
  def create(name: ItemName): F[Unit]
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
}
