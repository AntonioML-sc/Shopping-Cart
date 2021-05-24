package shop.http.routes

import cats.{ Defer, Monad }
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import shop.services.HealthCheck
import shop.http.json._

final class HealthCheckRoutes[F[_]: Defer: Monad](healthCheck: HealthCheck[F]) extends Http4sDsl[F] {
  private[routes] val prefixPath: String = "/healthcheck"
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root => Ok(healthCheck.status)
  }
  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)
}

object HealthCheckRoutes {
  def apply[F[_]: Defer: Monad](hCR: HealthCheck[F]): HealthCheckRoutes[F] = new HealthCheckRoutes[F](hCR)
}
