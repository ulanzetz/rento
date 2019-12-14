package com.github.ulanzetz.rento.misc

trait PasswordHasher {
  def hash(password: String): String
}

object PasswordHasher {
  class SHA1(salt: String) extends PasswordHasher {
    val md = java.security.MessageDigest.getInstance("SHA-1")

    def hash(password: String): String =
      md.digest((password + salt).getBytes("UTF-8")).map("%02x".format(_)).mkString
  }
}
