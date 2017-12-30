package models

import akka.actor.ActorRef
import java.util.UUID

object MessageModels {
  case class SetPlayer(playerRef: ActorRef, uuid: UUID)
  case class StartGame()
  case class UpdateMap(map: String)
  case class SpreadTroops(player: String, troops: Int)
}