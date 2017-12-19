package services.game

import models.GameModel

import akka.actor._
import play.api.libs.json._
import akka.actor.PoisonPill
import scala.swing.Reactor

object MessageTypes extends Enumeration {
  type MessageTypes = Value
  val MessageTypeList, UpdateMap, Close, Click, Unknown = Value

  def stringToValue(messageType: String):Option[MessageTypes] = values.find(_.toString.equals(messageType))
}

class SocketActor(out: ActorRef) extends Actor with Reactor {
    listenTo(context)

    def reactor = {
      case _ => println("bar")
    }

    def receive = {
      case msg: String => {
        val json: JsValue = Json.parse(msg)
        (json \ "type").asOpt[String] match {
          case None => sendMessageTypes
          case Some(messageType) => MessageTypes.stringToValue(messageType) match {
            case None => println("No type: "+ msg + ", " + MessageTypes.stringToValue(msg))
            case Some(messageTypeValue) => messageTypeValue match {
              case MessageTypes.MessageTypeList => sendMessageTypes
              case MessageTypes.Close => self ! PoisonPill
              case MessageTypes.Click => (json \ "message").asOpt[String].getOrElse("") // TODO: implement game logic

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

    def closeActor = self ! PoisonPill
  }
