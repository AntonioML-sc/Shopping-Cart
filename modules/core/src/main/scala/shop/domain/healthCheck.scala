package shop.domain

import io.estatico.newtype.macros.newtype

object healthCheck {

  case class AppStatus(postgres: PostgresStatus)

  @newtype case class PostgresStatus(value: Boolean)

}
