package com.github.ulanzetz.rento.repositories

import com.github.ulanzetz.rento.domain.{DateTime, Time}

object Instances {
  implicit val timeMeta: doobie.Meta[Time] =
    doobie.Meta.TimeMeta.imap(_.toLocalTime)(java.sql.Time.valueOf)

  implicit val dateTimeMeta: doobie.Meta[DateTime] =
    doobie.Meta.TimestampMeta.imap(_.toLocalDateTime)(java.sql.Timestamp.valueOf)
}
