package shop.services

import cats.effect.{ Concurrent, Resource, Sync, Timer }
import cats.effect.implicits._
import cats.implicits._
import shop.domain.healthCheck.{ AppStatus, PostgresStatus }
import shop.services.HealthCheckSQL.query
import skunk.codec.all.int4
import skunk.implicits.toStringOps
import skunk._

import scala.concurrent.duration.DurationInt

trait HealthCheck[F[_]] {
  def status: F[AppStatus]
}

object HealthCheck {
  def make[F[_]: Concurrent: Timer](sessionPool: Resource[F, Session[F]]): F[HealthCheck[F]] =
    Sync[F].delay(new HealthCheckLive[F](sessionPool))
}

final class HealthCheckLive[F[_]: Concurrent: Timer](sessionPool: Resource[F, Session[F]]) extends HealthCheck[F] {
  override def status: F[AppStatus] = postgresHealth.map(AppStatus.apply)

  val postgresHealth: F[PostgresStatus] = sessionPool
    .use(session => session.execute(query))
    .map(_.nonEmpty)
    .timeout(1.second)
    .orElse(false.pure[F])
    .map(PostgresStatus.apply)

}

object HealthCheckSQL {
  val query: Query[Void, Int] =
    sql"""
         SELECT pid FROM pg_stat_activity
         """.query(int4)
}
