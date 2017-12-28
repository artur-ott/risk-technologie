package services.game

import models.GameModel

import akka.actor._
import play.api.libs.json._
import akka.actor.PoisonPill

object MessageTypes extends Enumeration {
  type MessageTypes = Value
  val MessageTypeList, Ping, StartGame, SpreadTroops, UpdateMap, Close, Click, Unknown = Value

  def stringToValue(messageType: String): Option[MessageTypes] = values.find(_.toString.equals(messageType))
}

case class Message(messageType: String, message: String = "\"\"") {
  def toJson: String = {
    val m_type = "\"type\":\"" + messageType + "\""
    val value = "\"value\": " + message
    ("{" + m_type + ", " + value + "}")
  }
}

class SocketActor(out: ActorRef, gameManager: ActorRef, user: String) extends Actor {
  override def preStart(): Unit = {
    gameManager ! models.MessageModels.SetPlayer(self, user)
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
            //case MessageTypes.Click => (json \ "message").asOpt[String].getOrElse("") // TODO: implement game logic
            // TODO: startGame
          }
        }
      }
    }
    case models.MessageModels.UpdateMap(map) => out ! Message("UpdateMap", map).toJson
    case models.MessageModels.SpreadTroops(player, troops) => out ! Message("SpreadTroops", "{\"player\": \"" +
      player + "\", \"troops\": \"" + troops + "\"}").toJson
    case _ => println("foo")
  }

  def sendMessageTypes() = {
    val messageType = "\"type\":\"" + MessageTypes.MessageTypeList + "\""
    val value = "\"value\": [\"" + MessageTypes.values.mkString("\", \"") + "\"]"
    out ! ("{" + messageType + ", " + value + "}")
  }
}
