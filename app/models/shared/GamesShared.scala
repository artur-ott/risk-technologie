package models.shared

import models.GameModel
import scala.collection.mutable._
import java.util.UUID

object GamesShared {
  private val games: ListBuffer[GameModel] = new ListBuffer()

  def getGames: List[GameModel] = this.games.result()

  def addGame(game: GameModel) = this.games += game;

  def removeGame(id: String) = this.games --= this.games.filter(_.id.equals(id))

  def getGameWithPlayer(uuid: UUID): Option[GameModel] = {
    this.games.filter(e => e.player.exists(p => p.uuid.equals(uuid))).headOption
  }
}