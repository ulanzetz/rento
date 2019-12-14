package com.github.ulanzetz.rento.services

import com.github.ulanzetz.rento.domain._
import com.github.ulanzetz.rento.repositories.RestaurantRepository

trait RestaurantService[F[_]] {
  def list: F[List[Restaurant]]

  def schedule(id: Long): F[List[RestaurantSchedule]]
}

object RestaurantService {
  class Live[F[_]](implicit repo: RestaurantRepository[F]) extends RestaurantService[F] {
    val list: F[List[Restaurant]] =
      repo.list

    def schedule(id: Long): F[List[RestaurantSchedule]] =
      repo.schedule(id)
  }
}
