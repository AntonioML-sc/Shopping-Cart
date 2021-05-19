package shop

import cats.effect.{ Concurrent, ContextShift, Resource }
import natchez.Trace.Implicits.noop
import skunk.Session

object AppResources {
  def make[F[_]: Concurrent: ContextShift]: Resource[F, AppResources[F]] =
    makeSession.map(session => AppResources(session))

  def makeSession[F[_]: Concurrent: ContextShift]: Resource[F, Resource[F, Session[F]]] =
    Session
      .pooled[F](
        host = "localhost",
        port = 5432,
        user = "postgres",
        database = "store",
        max = 10
      )
}

final case class AppResources[F[_]] private (session: Resource[F, Session[F]])
