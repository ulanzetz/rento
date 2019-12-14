package com.github.ulanzetz.rento.ui

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, FormatStyle}
import java.util.Locale

import cats.{~>, Id, Monad}
import cats.implicits._
import com.github.ulanzetz.rento.context.AppContext
import com.github.ulanzetz.rento.domain._
import com.github.ulanzetz.rento.misc.TimeProvider
import com.github.ulanzetz.rento.services.OrderService
import com.github.ulanzetz.rento.state._
import levsha.dsl._
import html._

class OrderModule[F[_]: Monad, G[_]](
    implicit val ctx: AppContext[F],
    orderService: OrderService[G],
    timeProvider: TimeProvider[G],
    runSync: G ~> Id,
    lift: G ~> F
) extends UIModule[F] {
  def render: PartialFunction[State, HTML[F]] = {
    case State(Page.OrderListPage, user: User.Authorized) => orderListPage(user)
  }

  private def orderListPage(user: User.Authorized): HTML[F] = {
    val nowDateTime = runSync(timeProvider.nowDateTime)

    body(
      navbar(user),
      div(
        align := "center",
        h4("Список заказов"),
        runSync(orderService.list(user.phone)).map(orderCard(_, nowDateTime))
      )
    )
  }

  private def orderCard(order: Order, nowDateTime: LocalDateTime): HTML[F] =
    div(
      `class` := "card",
      img(
        `class` := "card-img-top",
        src := order.restaurant.photo,
        ctx.event("click")(_.transition(_.copy(page = Page.RestaurantPage(order.restaurant, nowDateTime.toLocalDate))))
      ),
      div(
        `class` := "card-body",
        h5(`class` := "card-title", order.restaurant.name),
        h6(
          `class` := "card-title",
          DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(new Locale("ru"))
            .format(order.dateTime)
        ),
        h6(`class` := "card-title", s"Количество персон: ${order.personCount}"),
        h6(`class` := "card-title", s"На имя: ${order.name}"),
        h6(`class` := "card-title", s"Телефон: ${order.phone}"),
        if (order.canceled)
          a(color @= "#FF0000", "Отменен")
        else if (order.dateTime.isBefore(nowDateTime))
          a(`class` := "btn btn-primary", "Оценить")
        else
          a(
            `class` := "btn btn-danger",
            "Отменить",
            ctx.event("click")(
              access =>
                for {
                  _ <- lift(orderService.cancelOrder(order.id))
                  _ <- access.transition(identity)
                } yield ()
            )
          )
      )
    )
}
