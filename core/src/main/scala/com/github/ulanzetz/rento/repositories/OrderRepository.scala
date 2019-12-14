package com.github.ulanzetz.rento.repositories

import java.time.LocalDateTime

import doobie.implicits._
import cats.effect.Sync
import cats.implicits._
import com.github.ulanzetz.rento.domain._
import com.github.ulanzetz.rento.repositories.Instances._
import doobie.util.transactor.Transactor

trait OrderRepository[F[_]] {
  def createOrder(restaurantId: Long, personCount: Int, dateTime: LocalDateTime, phone: Phone, name: String): F[Unit]

  def cancelOrder(orderId: Long): F[Unit]

  def list(phone: Phone): F[List[Order]]
}

object OrderRepository {
  class Live[F[_]: Sync](implicit xa: Transactor[F]) extends OrderRepository[F] {
    def createOrder(
        restaurantId: Long,
        personCount: Int,
        dateTime: LocalDateTime,
        phone: Phone,
        name: String
    ): F[Unit] =
      sql"""insert into orders (phone, name, person_count, datetime, restaurant_id) 
            values ($phone, $name, $personCount, $dateTime, $restaurantId)""".update.run
        .transact(xa)
        .void

    def cancelOrder(orderId: Long): F[Unit] =
      sql"update orders set canceled = true where id = $orderId".update.run.transact(xa).void

    def list(phone: Phone): F[List[Order]] =
      sql"""select o.id, o.phone, o.name, r.id, r.name, r.description, 
            r.photo, r.address, r.rating, r.phone, r.middle_check,
            o.person_count, o.datetime, o.canceled
            from orders as o join restaurants r on o.restaurant_id = r.id where o.phone = $phone
            order by datetime desc"""
        .query[Order]
        .to[List]
        .transact(xa)
  }
}
