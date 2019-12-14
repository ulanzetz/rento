package com.github.ulanzetz.rento.components

import com.github.ulanzetz.rento.config.ServicesConfig
import com.github.ulanzetz.rento.misc.PasswordHasher
import com.github.ulanzetz.rento.services._
import zio.Task
import zio.interop.catz._

final class ServiceComponent(
    implicit val authService: AuthService[Task],
    implicit val restaurantService: RestaurantService[Task],
    implicit val profileService: ProfileService[Task],
    implicit val orderSerivice: OrderService[Task]
)

object ServiceComponent {
  def apply(cfg: ServicesConfig)(implicit repos: RepositoryComponent): ServiceComponent = {
    import repos._

    implicit val passowordHasher: PasswordHasher = new PasswordHasher.SHA1(cfg.passwordSalt)

    implicit val authService: AuthService[Task] = new AuthService.Live

    implicit val restaurantService: RestaurantService[Task] = new RestaurantService.Live

    implicit val profileService: ProfileService[Task] = new ProfileService.Live

    implicit val orderService: OrderService[Task] = new OrderService.Live

    new ServiceComponent
  }
}
