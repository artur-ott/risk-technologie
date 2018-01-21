package models

import java.util.UUID

case class PlayerModel(uuid: UUID, name: String, var color: String = "WHITE", var startedGame: Boolean = false)