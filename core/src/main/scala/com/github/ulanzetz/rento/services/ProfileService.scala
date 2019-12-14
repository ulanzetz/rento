package com.github.ulanzetz.rento.services

import cats.Functor
import cats.implicits._
import com.github.ulanzetz.rento.domain.Phone
import com.github.ulanzetz.rento.misc.PasswordHasher
import com.github.ulanzetz.rento.repositories.AccountRepository

trait ProfileService[F[_]] {
  def updateName(phone: Phone, name: String): F[Unit]

  def updatePassword(phone: Phone, password: String): F[Unit]

  def rightPassword(phone: Phone, password: String): F[Boolean]
}

object ProfileService {
  class Live[F[_]: Functor](implicit accountRepo: AccountRepository[F], passwordHasher: PasswordHasher)
    extends ProfileService[F] {
    def updateName(phone: Phone, name: String): F[Unit] =
      accountRepo.updateName(phone, name)

    def updatePassword(phone: Phone, password: String): F[Unit] =
      accountRepo.updatePasswordHash(phone, passwordHasher.hash(password))

    def rightPassword(phone: Phone, password: String): F[Boolean] =
      accountRepo.passwordHash(phone).map(_ == passwordHasher.hash(password))
  }
}
