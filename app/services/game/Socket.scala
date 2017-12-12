package services.game

import akka.actor._
import play.api.libs.json._

object MessageTypes extends Enumeration {
  type MessageTypes = Value
  val MessageTypeList, UpdateMap, Unknown = Value

  def stringToValue(messageType: String):Option[MessageTypes] = values.find(_.toString.equals(messageType))
}

class SocketActor(out: ActorRef) extends Actor {
    def receive = {
      case msg: String => {
        val json: JsValue = Json.parse(msg)
        (json \ "type").asOpt[String] match {
          case None => sendMessageTypes
          case Some(messageType) => MessageTypes.stringToValue(messageType) match {
            case None => println("No type: "+ msg + ", " + MessageTypes.stringToValue(msg))
            case Some(messageTypeValue) => messageTypeValue match {
              case MessageTypes.MessageTypeList => sendMessageTypes
            }
          }
        }
      }
    }

    def sendMessageTypes = {
      val messageType = "\"type\":\"" + MessageTypes.MessageTypeList + "\""
      val value = "\"value\": [\"" + MessageTypes.values.mkString("\", \"") + "\"]"
      out ! ("{" + messageType + ", " + value + "}")
    }
  }
