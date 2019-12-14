package com.github.ulanzetz.rento.ui

import java.time.{LocalDate, LocalDateTime, LocalTime}

import cats.{~>, Id, Monad}
import cats.implicits._
import com.github.ulanzetz.rento.context.AppContext
import com.github.ulanzetz.rento.domain._
import com.github.ulanzetz.rento.misc.TimeProvider
import com.github.ulanzetz.rento.services.{OrderService, RestaurantService}
import com.github.ulanzetz.rento.state._
import levsha.dsl._
import html._

class RestaurantModule[F[_]: Monad, G[_]](
    implicit val ctx: AppContext[F],
    restaurantService: RestaurantService[G],
    orderService: OrderService[G],
    timeProvider: TimeProvider[G],
    runSync: G ~> Id,
    lift: G ~> F
) extends UIModule[F] {
  def render: PartialFunction[State, HTML[F]] = {
    case State(Page.RestaurantListPage, user: User.Authorized) => restaurantListPage(user)
    case State(page: Page.RestaurantPage, user: User.Authorized) =>
      restaurantPage(user, page)
  }

  private def restaurantListPage(user: User.Authorized): HTML[F] =
    body(
      navbar(user),
      div(`class` := "row", runSync(restaurantService.list).map(card(_, runSync(timeProvider.nowDate))))
    )

  private def card(restaurant: Restaurant, nowDate: LocalDate): HTML[F] = {
    val clickHandler = ctx.event("click")(_.transition(_.copy(page = Page.RestaurantPage(restaurant, nowDate))))

    div(
      `class` := "col-sm-4",
      div(
        `class` := "card",
        img(`class` := "card-img-top", src := restaurant.photo, clickHandler),
        div(
          `class` := "card-body",
          h5(`class` := "card-title", restaurant.name),
          p(`class` := "card-subtitle", restaurant.address),
          p(`class` := "card-subtitle", s"Рейтинг: ${restaurant.rating}"),
          p(`class` := "card-text", restaurant.description),
          a(`class` := "btn btn-primary", "Забронировать", clickHandler)
        )
      ),
      marginBottom @= "10px"
    )
  }

  private def restaurantPage(user: User.Authorized, page: Page.RestaurantPage): HTML[F] = {
    import page._

    val schedule    = runSync(restaurantService.schedule(restaurant.id))
    val nowDateTime = runSync(timeProvider.nowDateTime)
    val nowDate     = nowDateTime.toLocalDate

    val personCountSelect = ctx.elementId("person_count".some)
    val dateSelect        = ctx.elementId("date".some)
    val timeSelect        = ctx.elementId("time".some)

    body(
      navbar(user),
      div(
        align := "center",
        h4(restaurant.name),
        img(src := restaurant.photo),
        p(""),
        p(s"Рейтинг: ${restaurant.rating}"),
        p(s"Средний чек: ${restaurant.middleCheck}"),
        p(s"Адрес: ${restaurant.address}"),
        p(s"Телефон: ${restaurant.phone}"),
        p(s"Часы работы: "),
        textSchedule(schedule),
        p(restaurant.description),
        h5("Забронировать стол:"),
        form(
          width @= "15%",
          div(
            `class` := "form-group",
            label("Количество персон:"),
            select(personCountSelect, `class` := "form-control", (1 to 8).map(count => option(count.toString)))
          ),
          div(
            `class` := "form-group",
            label("Выберите дату:"),
            small(`class` := "form-text text-muted", "Бронирование доступно только на 2 недели вперед"),
            select(
              dateSelect,
              `class` := "form-control",
              value := selectedDate.toString,
              (0 to 14).map(plus => option(nowDate.plusDays(plus).toString)),
              ctx.event("change") { access =>
                for {
                  date <- access.valueOf(dateSelect).map(LocalDate.parse)
                  _    <- access.transition(_.copy(page = page.copy(selectedDate = date)))
                } yield ()
              }
            )
          ),
          div(
            `class` := "form-group",
            label("Выберите время:"),
            select(timeSelect, `class` := "form-control", timeSlots(schedule, selectedDate, nowDateTime))
          ),
          button(`class` := "btn btn-primary", "Забронировать"),
          ctx.event("submit") { access =>
            for {
              personCount <- access.valueOf(personCountSelect).map(_.toInt)
              time        <- access.valueOf(timeSelect).map(LocalTime.parse)
              _ <- lift(
                orderService.createOrder(
                  restaurant.id,
                  personCount,
                  LocalDateTime.of(page.selectedDate, time),
                  user.phone,
                  user.name
                )
              )
              _ <- access.transition(_.copy(page = Page.OrderListPage))
            } yield ()
          }
        )
      )
    )
  }

  private def timeSlots(
      schedule: List[RestaurantSchedule],
      selectedDate: LocalDate,
      nowDateTime: LocalDateTime
  ): List[HTML[F]] = {
    val dayOfWeak     = selectedDate.getDayOfWeek.getValue
    val prevDayOfWeak = selectedDate.minusDays(1).getDayOfWeek.getValue
    val midnight      = LocalTime.of(0, 0)

    val yest =
      schedule.find(_.weekday == prevDayOfWeak) match {
        case Some(sc) if sc.end.isAfter(midnight) =>
          (midnight.plusMinutes(30), sc.end.minusMinutes(1)).some
        case _ => None
      }

    val tod = schedule
      .find(_.weekday == dayOfWeak)
      .map(d => (d.start, if (d.start.isAfter(d.end)) midnight.minusMinutes(1) else d.end.minusMinutes(1)))

    val union = yest.toList ::: tod.toList

    union match {
      case h :: _ =>
        var start = if (nowDateTime.toLocalDate == selectedDate) {
          val nowPlusSub = nowDateTime.toLocalTime.plusMinutes(30).withSecond(0).withNano(0)

          if (nowPlusSub.getMinute > 30) nowPlusSub.plusMinutes(60 - nowPlusSub.getMinute)
          else nowPlusSub.withMinute(30)
        } else h._1

        var current = union
        val slots   = new collection.mutable.ListBuffer[LocalTime]

        while (current != Nil) {
          if (start.isBefore(current.head._1) && start != midnight) start = current.head._1
          if (start.isAfter(current.head._2) || start == midnight)
            current = current.tail
          else {
            slots += start
            start = start.plusMinutes(30)
          }
        }

        slots.toList.map(sl => option(sl.toString))
      case Nil => Nil
    }
  }

  private def textSchedule(restaurantSchedule: List[RestaurantSchedule]): List[HTML[F]] =
    restaurantSchedule
      .groupBy(schedule => (schedule.start, schedule.end))
      .toList
      .sortBy(_._2.map(_.weekday).min)
      .map {
        case ((start: Time, end: Time), days: List[RestaurantSchedule]) =>
          p(s"${days.map(day => textWeekday(day.weekday)).mkString(",")}: $start - $end")
      }

  private def textWeekday(number: Int): String =
    number match {
      case 1 => "Пн"
      case 2 => "Вт"
      case 3 => "Ср"
      case 4 => "Чт"
      case 5 => "Пт"
      case 6 => "Сб"
      case 7 => "Вс"
    }
}
