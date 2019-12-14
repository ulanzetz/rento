scalaVersion in ThisBuild := "2.13.1"

scalacOptions in ThisBuild ++= Seq(
  "-encoding",
  "utf-8",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Ywarn-dead-code",
  "-Ywarn-unused:imports",
  "-Ywarn-unused:locals",
  "-Ywarn-unused:params,-implicits",
  "-Ywarn-unused:patvars",
  "-Ywarn-unused:privates",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:postfixOps",
  "-language:implicitConversions"
)

val doobieVersion = "0.8.6"

val doobie = Seq(
  "org.tpolecat" %% "doobie-core"     % doobieVersion,
  "org.tpolecat" %% "doobie-hikari"   % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion
)

val korolevVersion = "0.14.0"

val korolev = Seq(
  "com.github.fomkin" %% "korolev"                     % korolevVersion,
  "com.github.fomkin" %% "korolev-cats-effect-support" % korolevVersion,
  "com.github.fomkin" %% "korolev-server-akkahttp"     % korolevVersion
)

val zio = Seq("dev.zio" %% "zio" % "1.0.0-RC17", "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC10")

val ficus = Seq("com.iheart" %% "ficus" % "1.4.7")

val logback = Seq("ch.qos.logback" % "logback-classic" % "1.2.3")

val core = project.settings(version := "0.0.1").settings(name := "rento-core").settings(libraryDependencies ++= doobie)

val root = (project in file("."))
  .settings(name := "rento")
  .settings(libraryDependencies ++= korolev ++ ficus ++ zio ++ logback)
  .dependsOn(core)
  .aggregate(core)
