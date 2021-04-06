package shop

import cats.effect._
import org.http4s.server.blaze._
import shop.modules.HttpApi

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {

    BlazeServerBuilder[IO](global)
      .bindHttp(8080, "localhost")
      .withHttpApp(HttpApi.httpApp[IO])
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }

}
