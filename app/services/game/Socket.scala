package services.game

import models.GameModel

import akka.actor._
import play.api.libs.json._
import java.util.UUID
import scala.collection.mutable.HashMap

object MessageTypes extends Enumeration {
  type MessageTypes = Value
  val MessageTypeList, Ping, SignIn, PlayerList, StartGame, SpreadTroops, PlayerAttacking, PlayerAttackingContinue, DicesRolled, PlayerConqueredCountry, ConqueredCountry, EndTurn, TransfereTroops, ResetTransfereTroops, TransfereTroopsSetFromLand, TransfereTroopsSetToLand, UpdateMap, Close, Click, MoveTroops, Unknown = Value

  def stringToValue(messageType: String): Option[MessageTypes] = values.find(_.toString.equals(messageType))
}

case class Message(messageType: String, var message: String = "") {
  private[this] val valueMap: HashMap[String, String] = new HashMap[String, String]
  private[this] val objectMap: HashMap[String, String] = new HashMap[String, String]
  message = "\"" + message + "\""

  def addValue(key: String, value: Any) = {
    this.valueMap(key) = value.toString
  }

  def addObject(key: String, value: Any) = {
    this.objectMap(key) = value.toString
  }

  def messageObject(value: String) = {
    this.message = value
  }

  def toJson: String = {
    val messageTypeJson = "\"type\":\"" + messageType + "\""
    val messageJson: StringBuilder = new StringBuilder
    messageJson.append("\"value\": " + message)
    if ((this.valueMap.size > 0 || this.objectMap.size > 0) && this.message.length == 2) {
      messageJson.setLength(0)
      messageJson.append("\"value\": {")
      this.valueMap.foreach(value => {
        messageJson.append("\"" + value._1 + "\": ")
        messageJson.append("\"" + value._2 + "\", ")
      })
      this.objectMap.foreach(value => {
        messageJson.append("\"" + value._1 + "\": ")
        messageJson.append(value._2 + ", ")
      })

      messageJson.setLength(messageJson.length - 2)
      messageJson.append("}")
    }
    ("{" + messageTypeJson + ", " + messageJson.toString() + "}")
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
            case MessageTypes.StartGame => gameManager ! models.MessageModels.StartGame(uuid)
            case MessageTypes.Click => gameManager ! models.MessageModels.ClickedLand(uuid, (json \ "message").asOpt[String].getOrElse(""))
            case MessageTypes.MoveTroops => gameManager ! models.MessageModels.MoveTroops(uuid, (json \ "message").asOpt[Int].getOrElse(1))
            case MessageTypes.TransfereTroops =>
              val landFrom = (json \ "message" \ "landFrom").asOpt[String].getOrElse("")
              val landTo = (json \ "message" \ "landTo").asOpt[String].getOrElse("")
              val troops = (json \ "message" \ "troops").asOpt[Int].getOrElse(1)
              gameManager ! models.MessageModels.DragTroops(uuid, landFrom, landTo, troops)
            case MessageTypes.ResetTransfereTroops => gameManager ! models.MessageModels.ResetTransfereTroops(uuid)
            case MessageTypes.EndTurn => gameManager ! models.MessageModels.EndTurn(uuid)
            case _ => println("Wrong message type: " + messageTypeValue)
          }
        }
      }
    }

    case models.MessageModels.PlayerList(list) =>
      val message = Message("PlayerList")
      val messageArray: StringBuilder = new StringBuilder
      messageArray.append("[");
      list.foreach(player => {
        messageArray.append("[\"");
        messageArray.append(player._1);
        messageArray.append("\", \"");
        messageArray.append(player._2);
        messageArray.append("\"], ");
      })
      messageArray.setLength(messageArray.length - 2)
      messageArray.append("]");
      message.messageObject(messageArray.toString)
      out ! message.toJson
    case models.MessageModels.UpdateMap(map) =>
      val message = Message("UpdateMap")
      message.messageObject(map)
      out ! message.toJson
    case models.MessageModels.SpreadTroops(player, troops) =>
      val message = Message("SpreadTroops")
      message.addValue("player", player.toString)
      message.addValue("troops", troops.toString)
      out ! message.toJson
    case models.MessageModels.PlayerAttack(player: String) => out ! Message("PlayerAttacking", player.toString).toJson
    case models.MessageModels.RolledDices(players, dices) =>
      val message = Message("DicesRolled")
      message.addObject("lands", this.players(players))
      message.addObject("dices", this.dices(dices))
      out ! message.toJson
    case models.MessageModels.PlayerConqueredCountry(troops) => out ! Message("PlayerConqueredCountry", troops.toString).toJson
    case models.MessageModels.ConqueredCountry(land) => out ! Message("ConqueredCountry", land).toJson
    case models.MessageModels.TransfereTroops(player) => out ! Message("TransfereTroops", player.toString).toJson
    case models.MessageModels.TransfereTroopsSetFromLand(land, troops) =>
      val message = Message("TransfereTroopsSetFromLand")
      message.addValue("land", land)
      message.addValue("troops", troops)
      out ! message.toJson
    case models.MessageModels.TransfereTroopsSetToLand(land) => out ! Message("TransfereTroopsSetToLand", land).toJson
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
