package com.github.ulanzetz.rento.repositories

import cats.effect.Sync
import com.github.ulanzetz.rento.domain._
import com.github.ulanzetz.rento.repositories.Instances._
import doobie.util.transactor.Transactor
import doobie.implicits._

trait RestaurantRepository[F[_]] {
  def list: F[List[Restaurant]]

  def schedule(id: Long): F[List[RestaurantSchedule]]
}

object RestaurantRepository {
  class Live[F[_]: Sync](implicit xa: Transactor[F]) extends RestaurantRepository[F] {
    val list: F[List[Restaurant]] =
      sql"select id, name, description, photo, address, rating, phone, middle_check from restaurants order by rating desc limit 100"
        .query[Restaurant]
        .to[List]
        .transact(xa)

    def schedule(id: Long): F[List[RestaurantSchedule]] =
      sql"select weekday, start_time, end_time from restaurant_schedule where restaurant_id = $id order by weekday"
        .query[RestaurantSchedule]
        .to[List]
        .transact(xa)
  }
}
