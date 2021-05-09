package shop

import cats.effect._
import org.http4s.server.blaze._
import shop.modules.{HttpApi, Services}

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    for {
      services <- Services.make[IO]
      httpApi  <- HttpApi.make[IO](services)
      _        <- BlazeServerBuilder[IO](global)
        .bindHttp(8080, "localhost")
        .withHttpApp(httpApi.httpApp)
        .serve
        .compile
        .drain
    } yield ExitCode.Success

//    HttpApi.httpApp[IO].flatMap { httpApp =>
//      BlazeServerBuilder[IO](global)
//        .bindHttp(8080, "localhost")
//        .withHttpApp(httpApp)
//        .serve
//        .compile
//        .drain
//        .as(ExitCode.Success)
//    }
  }

}
