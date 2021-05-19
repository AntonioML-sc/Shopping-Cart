package shop.services

import cats.effect.{ Bracket, BracketThrow, Resource }
import cats.implicits._
import shop.domain.category.{ Category, CategoryId, CategoryName }
import shop.services.CategorySQL.{ insertCategory, selectAll }
import skunk._
import skunk.codec.all._
import skunk.{ Codec, Command, Query, Session }
import skunk.implicits._

import java.util.UUID

trait Categories[F[_]] {
  def findAll: F[List[Category]]
  def create(name: CategoryName): F[Unit]
}

object Categories {
  def make[F[_]: BracketThrow](sessionPool: Resource[F, Session[F]]): F[Categories[F]] =
    Bracket[F, Throwable].pure(new LiveCategories[F](sessionPool))
}

final class LiveCategories[F[_]: BracketThrow](sessionPool: Resource[F, Session[F]]) extends Categories[F] {
  override def findAll: F[List[Category]] = sessionPool.use(session => session.execute(selectAll))

  override def create(name: CategoryName): F[Unit] =
    sessionPool.use { session =>
      session.prepare(insertCategory).use(cmd => cmd.execute(name.toCategory(CategoryId(UUID.randomUUID()))).void)
    }
}

private object CategorySQL {
  val categoryId: Codec[CategoryId] = uuid.imap[CategoryId](uuid => CategoryId(uuid))(cId => cId.value)
  val categoryName: Codec[CategoryName] =
    varchar.imap[CategoryName](varchar => CategoryName(varchar))(cName => cName.value)
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
}
