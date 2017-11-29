package services.game

import akka.actor._

class SocketActor(out: ActorRef) extends Actor {
    def receive = {
      case msg: String =>
        out ! ("foo")
        println("Sent Json to Client: "+ msg)
    }

    def sendJsonToClient = {
      println("Received event from Controller")
      out ! ("bar")
    }
  }
