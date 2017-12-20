package models

import akka.actor.Props

object MessageModels {
    case class SetPlayer(prop:Props, user: String)
    case class StartGame()
    case class UpdateMap(map: String)
}