import sbt._

object Dependencies {
  object Versions {
    val cats       = "2.2.0"
    val catsEffect = "2.2.0"
    val circe      = "0.13.0"
    val fs2        = "2.4.6"
    val http4s     = "0.21.13"
    val newtype    = "0.4.3"
    val refined    = "0.9.19"
    val skunk      = "0.0.24"
    val squants    = "1.8.0"

    val betterMonadicFor = "0.3.1"
    val kindProjector    = "0.11.3"
  }
  object Libraries {
    def circe(artifact: String): ModuleID  = "io.circe"   %% artifact % Versions.circe
    def http4s(artifact: String): ModuleID = "org.http4s" %% artifact % Versions.http4s

    val cats       = "org.typelevel" %% "cats-core"   % Versions.cats
    val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect
    val fs2        = "co.fs2"        %% "fs2-core"    % Versions.fs2

    val circeCore    = circe("circe-core")
    val circeGeneric = circe("circe-generic")
    val circeParser  = circe("circe-parser")
    val circeRefined = circe("circe-refined")

    val http4sDsl    = http4s("http4s-dsl")
    val http4sServer = http4s("http4s-blaze-server")
    val http4sCirce  = http4s("http4s-circe")

    val newtype = "io.estatico" %% "newtype" % Versions.newtype

    val refinedCore = "eu.timepit" %% "refined"      % Versions.refined
    val refinedCats = "eu.timepit" %% "refined-cats" % Versions.refined

    val squants = "org.typelevel" %% "squants" % Versions.squants

    // Database
    val skunkCore  = "org.tpolecat" %% "skunk-core"  % Versions.skunk
    val skunkCirce = "org.tpolecat" %% "skunk-circe" % Versions.skunk

    // Compiler plugins
    val betterMonadicFor = "com.olegpy"    %% "better-monadic-for" % Versions.betterMonadicFor
    val kindProjector    = "org.typelevel" % "kind-projector"      % Versions.kindProjector
  }

}
