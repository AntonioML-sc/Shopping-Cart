package shop.services

import cats.effect.{ Bracket, BracketThrow, Resource }
import cats.implicits._
import shop.domain.category.{ Category, CategoryId, CategoryName, NewCategoryName, OldCategoryName, RenameCatInfo }
import shop.effects.GenUUID
import shop.services.CategorySQL.{
  deleteAll,
  deleteCategoryById,
  deleteCategoryByName,
  insertCategory,
  renameCategory,
  selectAll
}
import skunk._
import skunk.codec.all._
import skunk.data.Completion.{ Delete, Insert, Update }
import skunk.{ Codec, Command, Query, Session }
import skunk.implicits._

trait Categories[F[_]] {
  def findAll: F[List[Category]]
  def create(name: CategoryName): F[Boolean]
  def deleteById(id: CategoryId): F[Boolean]
  def deleteByName(name: CategoryName): F[Boolean]
  def clearAll: F[Boolean]
  def rename(renameInfo: RenameCatInfo): F[Boolean]
}

object Categories {
  def make[F[_]: BracketThrow: GenUUID](sessionPool: Resource[F, Session[F]]): F[Categories[F]] =
    Bracket[F, Throwable].pure(new LiveCategories[F](sessionPool))
}

final class LiveCategories[F[_]: BracketThrow: GenUUID](sessionPool: Resource[F, Session[F]]) extends Categories[F] {
  override def findAll: F[List[Category]] = sessionPool.use(session => session.execute(selectAll))

  override def create(name: CategoryName): F[Boolean] =
    sessionPool.use { session =>
      session
        .prepare(insertCategory)
        .use { cmd =>
          GenUUID[F].make.flatMap(uuid => cmd.execute(name.toCategory(CategoryId(uuid)))).map {
            case Insert(1) => true
            case _         => false
          }
        }
    }

  override def deleteById(id: CategoryId): F[Boolean] =
    sessionPool.use { session =>
      session
        .prepare(deleteCategoryById)
        .use { cmd =>
          cmd.execute(id).map {
            case Delete(0) => false
            case Delete(_) => true
            case _         => false
          }
        }
    }

  override def deleteByName(name: CategoryName): F[Boolean] =
    sessionPool.use { session =>
      session
        .prepare(deleteCategoryByName)
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

  override def rename(renameInfo: RenameCatInfo): F[Boolean] =
    sessionPool.use { session =>
      session
        .prepare(renameCategory)
        .use { cmd =>
          cmd.execute(renameInfo).map {
            case Update(1) => true
            case _         => false
          }
        }
    }

}

private object CategorySQL {
  val categoryId: Codec[CategoryId] = uuid.imap[CategoryId](uuid => CategoryId(uuid))(cId => cId.value)
  val categoryName: Codec[CategoryName] =
    varchar.imap[CategoryName](varchar => CategoryName(varchar))(cName => cName.value)
  val newCategoryName: Codec[NewCategoryName] =
    varchar.imap[NewCategoryName](varchar => NewCategoryName(varchar))(cName => cName.value)
  val oldCategoryName: Codec[OldCategoryName] =
    varchar.imap[OldCategoryName](varchar => OldCategoryName(varchar))(cName => cName.value)
  val category: Codec[Category] =
    (categoryId ~ categoryName).imap[Category] {
      case cId ~ cName => Category(cId, cName)
    }(c => c.uuid ~ c.name)
  val selectAll: Query[Void, Category] =
    sql"""
         SELECT * FROM categories
       """.query(category)
  val insertCategory: Command[Category] =
    sql"""
         INSERT INTO categories
         values ($category)
       """.command
  val deleteCategoryById: Command[CategoryId] =
    sql"""
         DELETE FROM categories
         WHERE uuid = $categoryId
       """.command
  val deleteCategoryByName: Command[CategoryName] =
    sql"""
         DELETE FROM categories
         WHERE name = $categoryName
       """.command
  val deleteAll: Command[Void] =
    sql"""
         DELETE FROM categories
       """.command
  val renameCategory: Command[RenameCatInfo] =
    sql"""
         UPDATE categories
         SET name = $newCategoryName
         WHERE name = $oldCategoryName
       """.command.contramap { case RenameCatInfo(newN, oldN) => newN ~ oldN }
}
