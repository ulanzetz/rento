package com.github.ulanzetz.rento

import java.time.LocalDate

import com.github.ulanzetz.rento.domain.{Phone, Restaurant}
import korolev.Context
import levsha.Document

package object state {
  type HTML[F[_]] = Document.Node[Context.Binding[F, State, Any]]

  case class State(page: Page, user: User)

  object State {
    val default = State(Page.PhonePage(), User.Unauthorized)
  }

  sealed trait Page

  object Page {
    case class PhonePage(phone: Option[Phone] = None, registered: Option[Boolean] = None) extends Page

    case object RestaurantListPage extends Page

    case class RestaurantPage(restaurant: Restaurant, selectedDate: LocalDate) extends Page

    case object ProfilePage extends Page

    case object OrderListPage extends Page
  }

  sealed trait User

  object User {
    case object Unauthorized extends User

    case class Authorized(phone: Phone, name: String) extends User
  }
}
