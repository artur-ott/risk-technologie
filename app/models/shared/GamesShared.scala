package models.shared

import models.GameModel
import scala.collection.mutable._

object GamesShared {
  private val games: ListBuffer[GameModel] = new ListBuffer()

  def getGames: List[GameModel] = this.games.result()

  def addGame(game: GameModel) = this.games += game;

  def removeGame(id: String) = this.games --= this.games.filter(_.id.equals(id))

  def getGameWithPlayer(name: String): Option[GameModel] = {
    val game = this.games.filter(e => e.player.exists(p => p.name.equals(name)))
    if (game.length > 0) {
      Some(game.head)
    } else {
      None
    }
  }
}