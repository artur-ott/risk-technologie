package models

import scala.collection.mutable._
import de.htwg.se.scala_risk.controller._

case class GameModel (val id: String, val player: ListBuffer[PlayerModel], var gameLogic: GameLogic)