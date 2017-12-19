package services.game

import scala.collection.mutable._
import akka.actor._

import models.PlayerModel
import models.MessageModels

import scala.swing.Publisher

import de.htwg.se.scala_risk.util.observer.TObserver
import de.htwg.se.scala_risk.util.Statuses
import de.htwg.se.scala_risk.controller.GameLogic

class GameManager(gameLogic: GameLogic, var players: ListBuffer[PlayerModel] = ListBuffer()) extends Actor with TObserver {
    gameLogic.add(this)

    def startGame = gameLogic.startGame

    def receive = {
        case SetPlayer(prop, user) => context.actorOf(prop, user)
        case _ => println("foo")
    }


    def update() {
        gameLogic.getStatus match {
            case Statuses.INITIALIZE_PLAYERS  =>  this.initializePlayers
            case Statuses.GAME_INITIALIZED => this.gameInitialized

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
        println(this.getMapdata)
        players.foreach(player => {

        })
    }


    def getMapdata: String = {
        val countries : scala.collection.mutable.ArrayBuffer[(String, String, Int, Int)] = gameLogic.getCountries
        var sb : StringBuilder = new StringBuilder

        val newline = "\n"
        sb.append("{" + newline)

        countries.foreach(country => {
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