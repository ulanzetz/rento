package com.github.ulanzetz.rento.ui

import cats.{~>, Monad}
import cats.implicits._
import com.github.ulanzetz.rento.context.AppContext
import com.github.ulanzetz.rento.services.ProfileService
import com.github.ulanzetz.rento.state._
import levsha.dsl._
import html._

class ProfileModule[F[_]: Monad, G[_]](implicit val ctx: AppContext[F], profileService: ProfileService[G], lift: G ~> F)
  extends UIModule[F] {
  def render: PartialFunction[State, HTML[F]] = {
    case State(Page.ProfilePage, user: User.Authorized) => profilePage(user)
  }

  def profilePage(user: User.Authorized): HTML[F] = {
    val nameInput        = ctx.elementId("name_input".some)
    val passwordInput    = ctx.elementId("password_input".some)
    val newPasswordInput = ctx.elementId("new_password_input".some)

    body(
      navbar(user),
      h5("Изменение имени"),
      form(
        div(
          `class` := "form-group",
          label("Имя"),
          width @= "15%",
          input(`class` := "form-control", nameInput, `type` := "text", name := "name", value := user.name),
        ),
        button(`class` := "btn btn-primary", "Изменить"),
        ctx.event("submit") { access =>
          for {
            newName <- access.valueOf(nameInput)
            _       <- lift(profileService.updateName(user.phone, newName))
            _       <- access.transition(_.copy(user = user.copy(name = newName)))
          } yield ()
        }
      ),
      p(),
      form(
        h5("Изменение пароля"),
        div(
          `class` := "form-group",
          label("Текущий пароль"),
          width @= "15%",
          input(`class` := "form-control", passwordInput, `type` := "password", name := "password", value := ""),
        ),
        div(
          `class` := "form-group",
          label("Новый пароль"),
          width @= "15%",
          input(`class` := "form-control", newPasswordInput, `type` := "password", name := "new_password", value := ""),
        ),
        button(`class` := "btn btn-primary", "Изменить"),
        ctx.event("submit") { access =>
          for {
            current <- access.valueOf(passwordInput)
            right   <- lift(profileService.rightPassword(user.phone, current))
            _ <- if (!right)
              access.evalJs("alert('Неверный пароль')").void
            else ().pure[F]
            newPassword <- access.valueOf(newPasswordInput)
            _           <- lift(profileService.updatePassword(user.phone, newPassword))
            _           <- access.evalJs("alert('Пароль изменен')")
          } yield ()
        }
      )
    )
  }
}
