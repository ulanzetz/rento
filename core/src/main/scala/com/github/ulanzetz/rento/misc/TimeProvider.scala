package com.github.ulanzetz.rento.misc

import java.time.{LocalDate, LocalDateTime, LocalTime}

import cats.Functor
import cats.implicits._
import cats.effect.Sync

abstract class TimeProvider[F[_]: Functor] {
  def nowDateTime: F[LocalDateTime]

  def nowDate: F[LocalDate] =
    nowDateTime.map(_.toLocalDate)

  def nowTime: F[LocalTime] =
    nowDateTime.map(_.toLocalTime)
}

object TimeProvider {
  class Live[F[_]: Sync] extends TimeProvider[F] {
    def nowDateTime: F[LocalDateTime] =
      Sync[F].delay(LocalDateTime.now())
  }
}
