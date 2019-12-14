package com.github.ulanzetz.rento

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}
import com.github.ulanzetz.rento.components.{ModuleComponent, RepositoryComponent, ServiceComponent}
import com.github.ulanzetz.rento.config.AppConfig
import com.github.ulanzetz.rento.context.AppContext
import com.github.ulanzetz.rento.state.{DeviceIdStateStorage, HTML, State}
import com.github.ulanzetz.rento.transactor.Transactor
import com.typesafe.config.ConfigFactory
import doobie.util.transactor.{Transactor => T}
import korolev.Context
import korolev.akkahttp.{AkkaHttpServerConfig, akkaHttpService}
import korolev.server.{KorolevServiceConfig, StateLoader}
import korolev.state.StateStorage
import korolev.state.javaSerialization._
import levsha.Id
import levsha.dsl._
import levsha.dsl.html._
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import zio.Task
import zio.internal.PlatformLive
import zio.interop.catz._

import scala.concurrent.{ExecutionContext, Future}

object EntryPoint {
  val typesafeConfig = ConfigFactory.load().resolve()

  val appConfig = typesafeConfig.as[AppConfig]("com.github.ulanzetz.rento")

  implicit val actorSystem: ActorSystem   = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()

  implicit val executionContext: ExecutionContext = actorSystem.dispatcher

  implicit val runtime: zio.Runtime[Unit] =
    zio.Runtime.apply((), PlatformLive.fromExecutionContext(actorSystem.dispatcher))

  implicit val appContext: AppContext[Future] = Context[Future, State, Any]

  implicit val transactor: T[Task] = Transactor(appConfig.db)

  implicit val repos: RepositoryComponent = RepositoryComponent.apply

  implicit val services: ServiceComponent = ServiceComponent.apply(appConfig.services)

  implicit val modules: ModuleComponent = ModuleComponent.apply

  val stateStorage: StateStorage[Future, State] = new DeviceIdStateStorage(new StateStorage.DefaultStateStorage)

  val stateLoader: StateLoader[Future, State] = (deviceId, _) =>
    for {
      exists <- stateStorage.exists(deviceId, "")
      res <- if (!exists)
        Future.successful(State.default)
      else stateStorage.get(deviceId, "").flatMap(_.read[State](Id.TopLevel).map(_.getOrElse(State.default)))
    } yield res

  val head: State => List[HTML[Future]] = _ => {
    List(
      link(
        rel := "stylesheet",
        href := "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/css/bootstrap.min.css",
        integrity := "sha384-rwoIResjU2yc3z8GV/NPeZWAv56rSmLldC3R/AZzGRnGxQQKnKkoFVhFQhNUwEyJ",
        crossorigin := "anonymous"
      ),
      style(
        """
          |html, body {
          |    max-width: 100%;
          |    overflow-x: hidden;
          |}
        """.stripMargin
      )
    )
  }

  val service =
    akkaHttpService[Future, State, Any](
      KorolevServiceConfig(
        stateLoader = stateLoader,
        stateStorage = stateStorage,
        head = head,
        render = modules.render
      )
    )

  def main(args: Array[String]): Unit = {
    Http().bindAndHandle(service(AkkaHttpServerConfig()), appConfig.server.host, appConfig.server.port)
    println(s"Server started on ${appConfig.server.host}:${appConfig.server.port}")
  }
}
