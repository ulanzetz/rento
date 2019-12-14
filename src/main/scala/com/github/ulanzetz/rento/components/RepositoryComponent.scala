package com.github.ulanzetz.rento.components

import com.github.ulanzetz.rento.repositories._
import doobie.util.transactor.Transactor
import zio.Task
import zio.interop.catz._

final class RepositoryComponent(
    implicit val accountRepo: AccountRepository[Task],
    implicit val restaurantRepo: RestaurantRepository[Task],
    implicit val orderRepo: OrderRepository[Task]
)

object RepositoryComponent {
  def apply(implicit xa: Transactor[Task]): RepositoryComponent = {
    implicit val accountRepo: AccountRepository[Task] = new AccountRepository.Live

    implicit val restaurantRepo: RestaurantRepository[Task] = new RestaurantRepository.Live

    implicit val orderRepo: OrderRepository[Task] = new OrderRepository.Live

    new RepositoryComponent
  }
}
