package models

import akka.actor.ActorRef

object MessageModels {
  case class SetPlayer(playerRef: ActorRef, user: String)
  case class StartGame()
  case class UpdateMap(map: String)
  case class SpreadTroops(player: String, troops: Int)
}