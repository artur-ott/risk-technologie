package models

import akka.actor.ActorRef
import java.util.UUID

object MessageModels {
  case class SetPlayer(playerRef: ActorRef, uuid: UUID)
  case class UpdateMap(map: String)
  case class SpreadTroops(player: String, troops: Int)
  case class PlayerAttack(player: String)
  case class RolledDices(players: (String, String), dices: (List[Int], List[Int]))
  case class ConqueredCountry(land: String)
  case class PlayerConqueredCountry(troops: Int)
  case class TransfereTroops(player: Boolean = false)
  case class TransfereTroopsSetFromLand(land: String, troops: Int)
  case class TransfereTroopsSetToLand(land: String)

  case class StartGame()
  case class ClickedLand(uuid: UUID, land: String)
  case class MoveTroops(uuid: UUID, troops: Int)
  case class EndTurn(uuid: UUID)
  case class ResetTransfereTroops(uuid: UUID)
  case class DragTroops(uuid: UUID, landFrom: String, landTo: String, troops: Int)
}