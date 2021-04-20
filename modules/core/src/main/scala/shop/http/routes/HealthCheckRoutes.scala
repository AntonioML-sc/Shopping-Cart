package shop.http.routes

import cats.{Defer, Monad}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final class HealthCheckRoutes[F[_] : Defer : Monad] extends Http4sDsl[F] {
  private[routes] val prefixPath: String = "/healthcheck"
  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root => Ok("MyHealthCheck")
  }
  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)
}

object HealthCheckRoutes {
  def apply[F[_] : Defer : Monad]: HealthCheckRoutes[F] = new HealthCheckRoutes[F]
}
