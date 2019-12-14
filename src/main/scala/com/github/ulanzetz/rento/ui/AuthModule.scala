package com.github.ulanzetz.rento.ui

import cats.{~>, Monad}
import cats.implicits._
import com.github.ulanzetz.rento.context.AppContext
import com.github.ulanzetz.rento.domain.Phone
import com.github.ulanzetz.rento.services.AuthService
import com.github.ulanzetz.rento.state._
import levsha.dsl._
import html._

class AuthModule[F[_]: Monad, G[_]](implicit val ctx: AppContext[F], authService: AuthService[G], lift: G ~> F)
  extends UIModule[F] {
  def render: PartialFunction[State, HTML[F]] = {
    case State(phone: Page.PhonePage, _) => phonePage(phone)
  }

  private def phonePage(page: Page.PhonePage): HTML[F] =
    page match {
      case Page.PhonePage(Some(phone), Some(true)) =>
        signIn(phone)
      case Page.PhonePage(Some(phone), Some(false)) =>
        signUp(phone)
      case _ =>
        phoneInput(page)
    }

  private def signIn(phone: Phone): HTML[F] = {
    val password = ctx.elementId("password".some)

    body(
      h4("Авторизация"),
      form(
        passwordForm(password),
        button(`class` := "btn btn-primary", "Продолжить"),
        ctx.event("submit") { access =>
          for {
            password   <- access.valueOf(password)
            optAccount <- lift(authService.signIn(phone, password))
            _ <- optAccount match {
              case Some(account) =>
                access.transition(_.copy(user = User.Authorized(phone, account.name), page = Page.RestaurantListPage))
              case None => access.evalJs("alert('Неверный пароль')").void
            }
          } yield ()
        }
      )
    )
  }

  private def signUp(phone: Phone): HTML[F] = {
    val password = ctx.elementId("password".some)
    val userName = ctx.elementId("userName".some)

    body(
      h4("Регистрация"),
      form(
        div(
          `class` := "form-group",
          label("Имя"),
          width @= "15%",
          input(`class` := "form-control", userName, `type` := "text", name := "name", value := ""),
        ),
        passwordForm(password),
        button(`class` := "btn btn-primary", "Продолжить"),
        ctx.event("submit") { access =>
          for {
            userName <- access.valueOf(userName)
            password <- access.valueOf(password)
            _        <- lift(authService.signUp(phone, userName, password))
            _        <- access.transition(_.copy(user = User.Authorized(phone, userName), page = Page.RestaurantListPage))
          } yield ()
        }
      )
    )
  }

  private def passwordForm(elementId: ctx.ElementId): HTML[F] =
    div(
      `class` := "form-group",
      label("Пароль"),
      width @= "15%",
      input(
        `class` := "form-control",
        elementId,
        `type` := "password",
        name := "password",
        pattern := "^.{6,}$",
        value := ""
      )
    )

  private def phoneInput(page: Page.PhonePage): HTML[F] = {
    val elementId = ctx.elementId("phone".some)

    body(
      h4("Введите номер телефона"),
      form(
        div(
          `class` := "form-group",
          label("Номер телефона"),
          width @= "15%",
          input(
            `class` := "form-control",
            elementId,
            `type` := "text",
            name := "phone",
            pattern := "^7[0-9]{10}$",
            value := ""
          ),
        ),
        button(`class` := "btn btn-primary", "Продолжить"),
        ctx.event("submit") { access =>
          for {
            phone      <- access.valueOf(elementId)
            registered <- lift(authService.containsPhone(phone))
            _          <- access.transition(_.copy(page = page.copy(registered = registered.some, phone = phone.some)))
          } yield ()
        }
      )
    )
  }
}
