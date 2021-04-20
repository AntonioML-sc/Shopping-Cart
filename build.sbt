import Dependencies._

ThisBuild / scalaVersion := "2.13.5"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "dev.antonio"
ThisBuild / organizationName := "Antonio"

lazy val root = (project in file("."))
  .settings(
    name := "shopping-cart"
  )
  .aggregate(core)

lazy val core = (project in file("modules/core"))
  .settings(
    scalacOptions += "-Ymacro-annotations",
    libraryDependencies ++= Seq(
      Libraries.cats,
      Libraries.catsEffect,
      Libraries.fs2,
      Libraries.circeCore,
      Libraries.circeGeneric,
      Libraries.circeParser,
      Libraries.http4sDsl,
      Libraries.http4sServer,
      Libraries.http4sCirce,
      Libraries.newtype,
      compilerPlugin(Libraries.kindProjector cross CrossVersion.full),
      compilerPlugin(Libraries.betterMonadicFor)
    )
  )