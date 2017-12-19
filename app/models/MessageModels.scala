package models

import akka.actor.Props

object MessageModels {
    case class SetPlayer(prop:Props, user: String)
}