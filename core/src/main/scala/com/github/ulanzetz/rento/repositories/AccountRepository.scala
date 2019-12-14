package com.github.ulanzetz.rento.repositories

import cats.implicits._
import cats.effect.Sync
import com.github.ulanzetz.rento.domain.{Account, Phone}
import doobie.util.transactor.Transactor
import doobie.implicits._

trait AccountRepository[F[_]] {
  def save(account: Account): F[Unit]

  def find(phone: Phone): F[Option[Account]]

  def updateName(phone: Phone, name: String): F[Unit]

  def updatePasswordHash(phone: Phone, passwordHash: String): F[Unit]

  def passwordHash(phone: Phone): F[String]
}

object AccountRepository {
  class Live[F[_]: Sync](implicit xa: Transactor[F]) extends AccountRepository[F] {
    def save(account: Account): F[Unit] =
      sql"""insert into accounts (phone, name, password_hash)
            values (${account.phone}, ${account.name}, ${account.passwordHash})""".update.run
        .transact(xa)
        .void

    def find(phone: Phone): F[Option[Account]] =
      sql"select phone, name, password_hash from accounts where phone = $phone".query[Account].option.transact(xa)

    def updateName(phone: Phone, name: String): F[Unit] =
      sql"update accounts set name = $name where phone = $phone".update.run.transact(xa).void

    def updatePasswordHash(phone: Phone, passwordHash: String): F[Unit] =
      sql"update accounts set password_hash = $passwordHash where phone = $phone".update.run.transact(xa).void

    def passwordHash(phone: Phone): F[String] =
      sql"select password_hash from accounts where phone = $phone".query[String].unique.transact(xa)
  }
}
