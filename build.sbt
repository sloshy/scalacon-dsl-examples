//Dependency versions
val catsV = "2.6.1"
val catsEffectV = "3.2.9"
val http4sV = "1.0.0-M29"
val munitV = "0.7.29"

//Dependencies
val cats = "org.typelevel" %% "cats-core" % catsV
val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectV
val http4sCirce = "org.http4s" %% "http4s-circe" % http4sV
val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sV
val http4sEmberServer = "org.http4s" %% "http4s-ember-server" % http4sV
val munit = "org.scalameta" %% "munit" % munitV % Test

val commonSettings = Seq(
  publish / skip := true,
  scalaVersion := "3.1.0"
)

val commonSettingsScala2 = Seq(
  publish / skip := true,
  scalaVersion := "2.13.7",
  addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)
)

lazy val root = (project in file("."))
  .settings(
    publish / skip := true
  )
  .aggregate(calculator, crudLang)

lazy val calculator = (project in file("examples/calculator"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      cats,
      munit
    )
  )

lazy val crudLang = (project in file("examples/crudlang"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      catsEffect,
      http4sCirce,
      http4sDsl,
      munit
    )
  )

lazy val crudLangApp = (project in file("examples/crudlang-app"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      http4sEmberServer,
      munit
    )
  )
  .dependsOn(crudLang)

lazy val calculatorScala2 = (project in file("examples-scala-2/calculator"))
  .settings(
    commonSettingsScala2,
    libraryDependencies ++= Seq(
      cats,
      munit
    )
  )

lazy val crudLangScala2 = (project in file("examples-scala-2/crudlang"))
  .settings(
    commonSettingsScala2,
    libraryDependencies ++= Seq(
      catsEffect,
      http4sCirce,
      http4sDsl,
      munit
    )
  )

lazy val crudLangAppScala2 = (project in file("examples-scala-2/crudlang-app"))
  .settings(
    commonSettingsScala2,
    libraryDependencies ++= Seq(
      http4sEmberServer,
      munit
    )
  )
  .dependsOn(crudLangScala2)
