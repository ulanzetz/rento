package com.github.ulanzetz.rento.components

import cats.implicits._
import cats.{Id, ~>}
import com.github.ulanzetz.rento.context.AppContext
import com.github.ulanzetz.rento.misc.TimeProvider
import com.github.ulanzetz.rento.state.{HTML, State}
import com.github.ulanzetz.rento.ui._
import levsha.dsl._
import levsha.dsl.html._
import zio.interop.catz._
import zio.{Exit, Task}

import scala.concurrent.{ExecutionContext, Future}

class ModuleComponent(modules: List[UIModule[Future]]) {
  def render(state: State): HTML[Future] =
    modules
      .foldLeft(PartialFunction.empty[State, HTML[Future]])((acc, next) => acc.orElse(next.render))
      .applyOrElse[State, HTML[Future]](state, _ => notFound)

  private val notFound: HTML[Future] =
    body(h1("Страница не найдена"))
}

object ModuleComponent {
  def apply(
      implicit services: ServiceComponent,
      appCtx: AppContext[Future],
      exCtx: ExecutionContext,
      runtime: zio.Runtime[Unit]
  ): ModuleComponent = {
    implicit val runTask: Task ~> Future = new (Task ~> Future) {
      def apply[A](fa: Task[A]): Future[A] =
        runtime.unsafeRunToFuture(fa)
    }

    implicit val runSync: Task ~> Id = new (Task ~> Id) {
      def apply[A](fa: Task[A]): Id[A] =
        runtime.unsafeRunSync(fa) match {
          case Exit.Success(success) => success
          case Exit.Failure(cause)   => throw new RuntimeException(s"Failed on run sync $cause")
        }
    }

    implicit val timeProvider: TimeProvider[Task] = new TimeProvider.Live[Task]

    import services._

    new ModuleComponent(
      List(
        new AuthModule[Future, Task],
        new RestaurantModule[Future, Task],
        new ProfileModule[Future, Task],
        new OrderModule[Future, Task]
      )
    )
  }
}
