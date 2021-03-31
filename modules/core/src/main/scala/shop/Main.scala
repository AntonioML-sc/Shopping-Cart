package shop

import cats.effect._
import org.http4s.implicits._
import org.http4s.server.blaze._
import shop.http.routes.BrandRoutes

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {

    BlazeServerBuilder[IO](global)
      .bindHttp(8080, "localhost")
      .withHttpApp(new BrandRoutes[IO].routes.orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

}
