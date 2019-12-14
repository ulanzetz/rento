package com.github.ulanzetz.rento

package object config {
  case class AppConfig(server: ServerConfig, db: DbConfig, services: ServicesConfig)

  case class ServerConfig(host: String, port: Int)

  case class DbConfig(driver: String, host: String, user: String, password: String, connectionPoolSize: Int)

  case class ServicesConfig(passwordSalt: String)
}
