package com.github.ulanzetz.rento.ui

import com.github.ulanzetz.rento.context.AppContext
import com.github.ulanzetz.rento.state.{HTML, Page, State, User}
import levsha.dsl._
import html._

trait UIModule[F[_]] {
  implicit val ctx: AppContext[F]

  def render: PartialFunction[State, HTML[F]]

  def navbar(user: User.Authorized): HTML[F] =
    nav(
      `class` := "navbar navbar-expand navbar-dark flex-column flex-md-row bd-navbar",
      a(`class` := "navbar-brand", href := "#", "Rento"),
      div(
        `class` := "navbar-nav-scroll",
        ul(
          `class` := "navbar-nav bd-navbar-nav flex-row",
          li(
            `class` := "nav-item",
            paddingLeft @= "30px",
            a(`class` := "nav-link", href := "#", "Рестораны", onClick(_.copy(page = Page.RestaurantListPage)))
          ),
          li(
            `class` := "nav-item",
            paddingLeft @= "30px",
            a(`class` := "nav-link", href := "#", "Мои заказы", onClick(_.copy(page = Page.OrderListPage)))
          ),
          li(
            `class` := "nav-item",
            paddingLeft @= "30px",
            a(`class` := "nav-link", href := "#", "Профиль", onClick(_.copy(page = Page.ProfilePage)))
          )
        )
      ),
      ul(
        `class` := "navbar-nav flex-row ml-md-auto d-none d-md-flex",
        li(
          `class` := "nav-item",
          paddingLeft @= "30px",
          a(
            `class` := "nav-link",
            href := "#",
            s"${user.name} +${user.phone}",
            onClick(_.copy(page = Page.ProfilePage))
          )
        ),
        li(
          `class` := "nav-item",
          paddingLeft @= "30px",
          a(`class` := "nav-link", href := "#", "Выйти", onClick(_ => State.default))
        )
      )
    )

  protected def onClick(transition: State => State): HTML[F] =
    ctx.event("click")(_.transition(transition))
}
