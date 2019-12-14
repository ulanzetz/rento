package com.github.ulanzetz.rento

import java.time.{LocalDateTime, LocalTime}

package object domain {
  type Phone    = String
  type Time     = LocalTime
  type DateTime = LocalDateTime

  case class Account(phone: Phone, name: String, passwordHash: String)

  case class Restaurant(
      id: Long,
      name: String,
      description: String,
      photo: String,
      address: String,
      rating: Double,
      phone: String,
      middleCheck: Int
  )

  case class RestaurantSchedule(weekday: Int, start: Time, end: Time)

  case class Order(
      id: Long,
      phone: Phone,
      name: String,
      restaurant: Restaurant,
      personCount: Int,
      dateTime: LocalDateTime,
      canceled: Boolean
  )
}
