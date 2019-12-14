package com.github.ulanzetz.rento.transactor

import java.util.concurrent.Executors

import cats.effect.{Async, Blocker, ContextShift}
import com.github.ulanzetz.rento.config.DbConfig
import com.zaxxer.hikari.HikariDataSource
import doobie.hikari.HikariTransactor
import doobie.{Transactor => T}

import scala.concurrent.ExecutionContext

object Transactor {
  def apply[F[_]: Async: ContextShift](cfg: DbConfig): T[F] = {
    val connectionPool = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(cfg.connectionPoolSize))

    val transactionPool = ExecutionContext.fromExecutor(Executors.newCachedThreadPool((r: Runnable) => {
      val t = new Thread(r, "transaction-pool")
      t.setDaemon(true)
      t
    }))

    val dataSource = new HikariDataSource()

    dataSource.setJdbcUrl(cfg.host)
    dataSource.setUsername(cfg.user)
    dataSource.setPassword(cfg.password)

    HikariTransactor(dataSource, connectionPool, Blocker.liftExecutionContext(transactionPool))
  }
}
