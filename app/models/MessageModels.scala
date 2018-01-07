package models

import akka.actor.ActorRef
import java.util.UUID

object MessageModels {
  case class SetPlayer(playerRef: ActorRef, uuid: UUID)
  case class UpdateMap(map: String)
  case class SpreadTroops(player: String, troops: Int)
  case class PlayerAttack(player: String)
  case class PlayerAttackingContinue()

  case class StartGame()
  case class ClickedLand(uuid: UUID, land: String)
}