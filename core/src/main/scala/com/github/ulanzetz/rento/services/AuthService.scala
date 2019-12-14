package com.github.ulanzetz.rento.services

import cats.Monad
import cats.implicits._
import com.github.ulanzetz.rento.domain.{Account, Phone}
import com.github.ulanzetz.rento.misc.PasswordHasher
import com.github.ulanzetz.rento.repositories.AccountRepository

trait AuthService[F[_]] {
  def containsPhone(phone: Phone): F[Boolean]

  def signUp(phone: Phone, name: String, password: String): F[Unit]

  def signIn(phone: Phone, password: String): F[Option[Account]]
}

object AuthService {
  class Live[F[_]: Monad](implicit accountRepo: AccountRepository[F], passwordHasher: PasswordHasher)
    extends AuthService[F] {
    def containsPhone(phone: Phone): F[Boolean] =
      accountRepo.find(phone).map(_.isDefined)

    def signUp(phone: Phone, name: String, password: String): F[Unit] =
      accountRepo.save(Account(phone, name, passwordHasher.hash(password)))

    def signIn(phone: Phone, password: String): F[Option[Account]] =
      accountRepo.find(phone).map {
        case Some(account) if account.passwordHash == passwordHasher.hash(password) =>
          account.some
        case _ => None
      }
  }
}
