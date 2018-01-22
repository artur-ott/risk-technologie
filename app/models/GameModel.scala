package models

import scala.collection.mutable._
import akka.actor.ActorRef

case class GameModel(val id: String, val player: ListBuffer[PlayerModel], var gameManager: Option[ActorRef], var gameStarted: Boolean = false)