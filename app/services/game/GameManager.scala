package services.game

import scala.collection.mutable._
import akka.actor._

import models.PlayerModel

import de.htwg.se.scala_risk.util.observer.TObserver
import de.htwg.se.scala_risk.util.Statuses
import de.htwg.se.scala_risk.controller.GameLogic

class GameManager(gameLogic: GameLogic, var players: ListBuffer[PlayerModel] = ListBuffer()) extends Actor with TObserver {
    var playerActorRefs:ListBuffer[(String, ActorRef)] = ListBuffer(); 
    gameLogic.add(this)

    def receive = {
        case models.MessageModels.SetPlayer(prop, user) => createPlayer(prop, user)
        case models.MessageModels.StartGame => startGame
        case _ => println("foo")
    }

    def createPlayer(prop: Props, user: String) = {
        this.getPlayerActorRef(user) match {
            case None => playerActorRefs += ((user, context.actorOf(prop)))
            case Some(child) => {
                context.stop(child._2)
                playerActorRefs - child
                playerActorRefs += ((user, context.actorOf(prop)))
            }
        }
    }

    def startGame = gameLogic.startGame

    def getPlayerActorRef(user:String):Option[(String, ActorRef)] = {
        playerActorRefs.filter(child => child._1.equals(user)).headOption
    }

    def update() {
        gameLogic.getStatus match {
            case Statuses.INITIALIZE_PLAYERS  =>  this.initializePlayers
            case Statuses.GAME_INITIALIZED => this.gameInitialized
            case Statuses.PLAYER_SPREAD_TROOPS => 

            case Statuses.NOT_ENOUGH_PLAYERS => {
                println(players.size)
            }

            case _ => println("Update: " + gameLogic.getStatus)
        }
    }

    def initializePlayers = {
        if (7 - gameLogic.getAvailableColors.size > players.size) gameLogic.initializeGame
        else {
            gameLogic.getAvailableColors.headOption match {
                case None => 
                case Some(color) => players.lift(6 - gameLogic.getAvailableColors.size) match {
                    case None => 
                    case Some(player) => gameLogic.setPlayer((player.name, color))
                }
            }
        }
    }

    def gameInitialized = {
        playerActorRefs.foreach(playerActorRef => {
            playerActorRef._2 ! models.MessageModels.UpdateMap(getMapdata)
        })
    }


    def getMapdata: String = {
        val countries : scala.collection.mutable.ArrayBuffer[(String, String, Int, Int)] = gameLogic.getCountries
        val countries_t = countries.map(country => {
            val colorHex = Integer.toHexString(gameLogic.getOwnerColor(country._2)).drop(1).drop(1)
            val colorInt = Integer.parseInt(colorHex, 16)
            val colorB = colorInt % 256
            val colorG = (colorInt / 256) % 256
            val colorR = ((colorInt / 256) / 256) % 256
            (country._1, country._2, country._3, "[" + colorR + ", " + colorG + ", " + colorB + "]")
        })
        var sb : StringBuilder = new StringBuilder

        val newline = "\n"
        sb.append("{" + newline)

        countries_t.foreach(country => {
            sb.append("\t\"" + country._1 + "\": {")
            sb.append(newline + "\t\t")
            sb.append("\"color\": " + country._4 + "," + newline + "\t\t")
            sb.append("\"troops\": " + country._3 + newline)
            sb.append("\t}," + newline)
        })

        sb = removeLastChar(sb)

        sb.append("}")

        sb.toString()
    }

    def removeLastChar (stringBuilder: StringBuilder) : StringBuilder = {
        val lastIndex = stringBuilder.lastIndexOf(",")
        val splits = stringBuilder.splitAt(lastIndex)
        val inputWithoutChar = splits._1.append(splits._2.drop(1))
        inputWithoutChar
    }
}