package services.game

import models.GameModel

import akka.actor._
import play.api.libs.json._
import java.util.UUID

object MessageTypes extends Enumeration {
  type MessageTypes = Value
  val MessageTypeList, Ping, StartGame, SpreadTroops, PlayerAttacking, PlayerAttackingContinue, DicesRolled, UpdateMap, Close, Click, Unknown = Value

  def stringToValue(messageType: String): Option[MessageTypes] = values.find(_.toString.equals(messageType))
}

case class Message(messageType: String, message: String = "\"\"") {
  def toJson: String = {
    val messageTypeJson = "\"type\":\"" + messageType + "\""
    val messageJson = "\"value\": " + message
    ("{" + messageTypeJson + ", " + messageJson + "}")
  }
}

class SocketActor(out: ActorRef, gameManager: ActorRef, uuid: UUID) extends Actor {
  override def preStart(): Unit = {
    gameManager ! models.MessageModels.SetPlayer(self, uuid)
    this.sendMessageTypes
  }

  def receive = {
    case msg: String => {
      val json: JsValue = Json.parse(msg)
      (json \ "type").asOpt[String] match {
        case None =>
        case Some(messageType) => MessageTypes.stringToValue(messageType) match {
          case None => println("No type: " + msg + ", " + MessageTypes.stringToValue(msg))
          case Some(messageTypeValue) => messageTypeValue match {
            case MessageTypes.Ping => out ! Message("Ping").toJson
            case MessageTypes.StartGame => gameManager ! models.MessageModels.StartGame
            case MessageTypes.Click => gameManager ! models.MessageModels.ClickedLand(uuid, (json \ "message").asOpt[String].getOrElse(""))
            case _ => println("Wrong message type: " + messageTypeValue)
          }
        }
      }
    }

    case models.MessageModels.UpdateMap(map) => out ! Message("UpdateMap", map).toJson
    case models.MessageModels.SpreadTroops(player, troops) => out ! Message("SpreadTroops", "{\"player\": \"" +
      player + "\", \"troops\": \"" + troops + "\"}").toJson
    case models.MessageModels.PlayerAttack(player: String) => out ! Message("PlayerAttacking", "\"" + player + "\"").toJson
    case models.MessageModels.RolledDices(players, dices) =>
      out ! Message("DicesRolled", "{ \"players\": " + this.players(players) + ", \"dices\": " + this.dices(dices) + "}").toJson
    case unknown: Any => println("Player: " + unknown.getClass.toString)
  }

  def players(players: (String, String)): String = {
    "[\"" + players._1 + "\",\"" + players._2 + "\"]"
  }

  def dices(dices: (List[Int], List[Int])): String = {
    "[[\"" + dices._1.mkString("\",\"") + "\"], [\"" + dices._2.mkString("\",\"") + "\"]]"
  }

  def sendMessageTypes() = {
    val messageType = "\"type\":\"" + MessageTypes.MessageTypeList + "\""
    val value = "\"value\": [\"" + MessageTypes.values.mkString("\", \"") + "\"]"
    out ! ("{" + messageType + ", " + value + "}")
  }
}
