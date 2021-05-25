package shop.services

import cats.effect.{ Bracket, BracketThrow, Resource }
import cats.implicits._
import io.estatico.newtype.macros.newtype
import shop.domain.item.{ Item, ItemId, ItemName }
import shop.services.ItemSQL.{
  deleteAll,
  deleteItemById,
  deleteItemByName,
  insertItem,
  modifyItemByName,
  selectAll,
  NewItemName,
  RenameInfo
}
import skunk._
import skunk.codec.all._
import skunk.data.Completion.{ Delete, Insert, Update }
import skunk.{ Codec, Command, Query }
import skunk.implicits._

import java.util.UUID

trait Items[F[_]] {
  def findAll: F[List[Item]]
  def create(name: ItemName): F[Boolean]
  def deleteById(id: ItemId): F[Boolean]
  def deleteByName(name: ItemName): F[Boolean]
  def clearAll: F[Boolean]
  def modifyByName(oldName: ItemName, newName: ItemName): F[Boolean]
}

object Items {
  def make[F[_]: BracketThrow](sessionPool: Resource[F, Session[F]]): F[Items[F]] =
    Bracket[F, Throwable].pure(new LiveItems[F](sessionPool))
}

final class LiveItems[F[_]: BracketThrow](sessionPool: Resource[F, Session[F]]) extends Items[F] {
  override def findAll: F[List[Item]] = sessionPool.use(session => session.execute(selectAll))

  override def create(name: ItemName): F[Boolean] =
    sessionPool.use { session =>
      session
        .prepare(insertItem)
        .use(cmd =>
          cmd.execute(name.toItem(ItemId(UUID.randomUUID()))).map {
            case Insert(1) => true
            case _         => false
          }
        )
    }

  override def deleteById(id: ItemId): F[Boolean] =
    sessionPool.use(session =>
      session
        .prepare(deleteItemById)
        .use(cmd =>
          cmd.execute(id).map {
            case Delete(0) => false
            case Delete(_) => true
            case _         => false
          }
        )
    )

  override def deleteByName(name: ItemName): F[Boolean] =
    sessionPool.use(session =>
      session
        .prepare(deleteItemByName)
        .use(cmd =>
          cmd.execute(name).map {
            case Delete(0) => false
            case Delete(_) => true
            case _         => false
          }
        )
    )

  override def clearAll: F[Boolean] =
    sessionPool.use(session =>
      session.execute(deleteAll).map {
        case Delete(0) => false
        case Delete(_) => true
        case _         => false
      }
    )

  override def modifyByName(oldName: ItemName, newName: ItemName): F[Boolean] =
    sessionPool.use(session =>
      session
        .prepare(modifyItemByName)
        .use(cmd =>
          cmd.execute(RenameInfo(oldName, NewItemName(newName.value))).map {
            case Update(1) => true
            case _         => false
          }
        )
    )
}

private object ItemSQL {
  val itemId: Codec[ItemId]           = uuid.imap[ItemId](uuid => ItemId(uuid))(iId => iId.value)
  val itemName: Codec[ItemName]       = varchar.imap[ItemName](varchar => ItemName(varchar))(iName => iName.value)
  val newItemName: Codec[NewItemName] = varchar.imap[NewItemName](varchar => NewItemName(varchar))(iName => iName.value)
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

  @newtype case class NewItemName(value: String)

  case class RenameInfo(oldName: ItemName, newName: NewItemName)

  val modifyItemByName: Command[RenameInfo] =
    sql"""
         UPDATE items
         SET name = $newItemName
         WHERE name = $itemName
       """.command.contramap { case RenameInfo(oldN, newN) => newN ~ oldN }
}
