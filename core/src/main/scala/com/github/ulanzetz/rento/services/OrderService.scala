package com.github.ulanzetz.rento.services

import java.time.LocalDateTime

import com.github.ulanzetz.rento.domain.{Order, Phone}
import com.github.ulanzetz.rento.repositories.OrderRepository

trait OrderService[F[_]] {
  def createOrder(restaurantId: Long, personCount: Int, dateTime: LocalDateTime, phone: Phone, name: String): F[Unit]

  def cancelOrder(orderId: Long): F[Unit]

  def list(phone: Phone): F[List[Order]]
}

object OrderService {
  class Live[F[_]](implicit repo: OrderRepository[F]) extends OrderService[F] {
    def createOrder(restaurantId: Long, personCount: Int, dateTime: LocalDateTime, phone: Phone, name: String): F[Unit] =
      repo.createOrder(restaurantId, personCount, dateTime, phone, name)

    def cancelOrder(orderId: Long): F[Unit] =
      repo.cancelOrder(orderId)

    def list(phone: Phone): F[List[Order]] =
      repo.list(phone)
  }
}
