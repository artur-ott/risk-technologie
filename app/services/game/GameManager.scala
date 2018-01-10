package services.game

import java.util.UUID

import scala.collection.mutable._
import akka.actor._

import models.PlayerModel

import de.htwg.se.scala_risk.util.observer.TObserver
import de.htwg.se.scala_risk.util.Statuses
import de.htwg.se.scala_risk.controller.GameLogic

class GameManager(gameLogic: GameLogic, var players: ListBuffer[PlayerModel] = ListBuffer()) extends Actor with TObserver {
  val playerActorRefs: ListBuffer[(UUID, ActorRef)] = ListBuffer();
  var attackingLand: String = "";
  gameLogic.add(this)

  def receive = {
    case models.MessageModels.SetPlayer(prop, uuid) => this.createPlayer(prop, uuid)
    case models.MessageModels.StartGame => this.startGame
    case models.MessageModels.ClickedLand(uuid, land) => this.clickedLand(uuid, land)
    case _ => println("GameManager: Unknown Message!")
  }

  def update() {
    gameLogic.getStatus match {
      case Statuses.INITIALIZE_PLAYERS => this.initializePlayers
      case Statuses.GAME_INITIALIZED => this.gameInitialized
      case Statuses.PLAYER_SPREAD_TROOPS => this.spreadTroops
      case Statuses.PLAYER_ATTACK => this.playerAttacking
      case Statuses.DIECES_ROLLED => this.rolledDices
      case Statuses.PLAYER_CONQUERED_A_COUNTRY => this.conqueredACountry

      case Statuses.NOT_ENOUGH_PLAYERS => {
        println("NOT_ENOUGH_PLAYERS: " + players.size)
      }

      case _ => println("Update: " + gameLogic.getStatus)
    }
  } /* case Statuses.PLAYER_SPREAD_TROOPS =>
        this.endTurnButton.setEnabled(false)
      case Statuses.PLAYER_ATTACK =>
        setStatusText("Angreifen")
        troopsToSpreadLabel.setVisible(false);
        this.endTurnButton.setEnabled(true)
        this.updateLabels()
        this.status = Statuses.PLAYER_ATTACK
      case Statuses.PLAYER_MOVE_TROOPS =>
        setStatusText("Verschieben")
        this.status = Statuses.PLAYER_MOVE_TROOPS
      case Statuses.DIECES_ROLLED => rollDices()
      case Statuses.PLAYER_CONQUERED_A_COUNTRY => if (this.running) {
        updateLabels()
        repaintCountry(
          gameLogic.getAttackerDefenderCountries._2._4,
          gameLogic.getOwnerColor(gameLogic.getAttackerDefenderCountries._1._2)
        )
        moveTroops()
      }
      case Statuses.PLAYER_CONQUERED_A_CONTINENT => if (this.running) {
        conqueredAContinent()
        updateLabels()
        repaintCountry(
          gameLogic.getAttackerDefenderCountries._2._4,
          gameLogic.getOwnerColor(gameLogic.getAttackerDefenderCountries._1._2)
        )
        moveTroops()
      }*/

  def createPlayer(playerRef: ActorRef, uuid: UUID) = {
    this.getPlayerActorRef(uuid) match {
      case None => playerActorRefs += ((uuid, playerRef))
      case Some(child) => {
        context.stop(child._2)
        playerActorRefs - child
        playerActorRefs += ((uuid, playerRef))
      }
    }
  }

  def currentPlayerName: String = {
    players.filter(p => p.uuid.toString().toUpperCase.equals(gameLogic.getCurrentPlayer._1.toUpperCase)).headOption match {
      case None => ""
      case Some(player) => player.name
    }
  }

  def startGame() = if (this.playerActorRefs.length >= 2) gameLogic.startGame

  def getPlayerActorRef(uuid: UUID): Option[(UUID, ActorRef)] = {
    this.getPlayerActorRef(uuid.toString)
  }

  def getPlayerActorRef(uuid: String): Option[(UUID, ActorRef)] = {
    this.playerActorRefs.filter(child => child._1.toString.toUpperCase.equals(uuid.toUpperCase)).headOption
  }

  def initializePlayers() = {
    if (7 - gameLogic.getAvailableColors.size > players.size) gameLogic.initializeGame
    else {
      gameLogic.getAvailableColors.headOption match {
        case None =>
        case Some(color) => players.lift(6 - gameLogic.getAvailableColors.size) match {
          case None =>
          case Some(player) => gameLogic.setPlayer((player.uuid.toString, color))
        }
      }
    }
  }

  def gameInitialized() = {
    this.playerActorRefs.foreach(playerActorRef => {
      playerActorRef._2 ! models.MessageModels.UpdateMap(getMapdata())
    })
  }

  def clickedLand(uuid: UUID, land: String) {
    if (uuid.toString().toUpperCase.equals(gameLogic.getCurrentPlayer._1.toUpperCase)) {
      gameLogic.getStatus match {
        case Statuses.PLAYER_SPREAD_TROOPS => gameLogic.addTroops(land, 1)
        case Statuses.PLAYER_ATTACK => this.attack(uuid, land)
        case _ => println("Land (" + land + ") clicked by (" + uuid + ") in wrong status(" + gameLogic.getStatus + ")")
      }
    }
  }

  def spreadTroops() = {
    val troops = gameLogic.getTroopsToSpread
    this.playerActorRefs.foreach(playerActorRef => {
      playerActorRef._2 ! models.MessageModels.SpreadTroops(currentPlayerName, troops)
      if (playerActorRef._1.toString.toUpperCase.equals(gameLogic.getCurrentPlayer._1.toUpperCase)) {
        playerActorRef._2 ! models.MessageModels.UpdateMap(getMapdata(2))
      } else {
        playerActorRef._2 ! models.MessageModels.UpdateMap(getMapdata())
      }
    })
  }

  def playerAttacking() = {
    this.playerActorRefs.foreach(playerActorRef => {
      playerActorRef._2 ! models.MessageModels.PlayerAttack(currentPlayerName)
      playerActorRef._2 ! models.MessageModels.UpdateMap(getMapdata(2))
    })
  }

  def attack(uuid: UUID, land: String) = {
    if (this.attackingLand.length == 0 && gameLogic.getOwnerName(land).equals(uuid.toString.toUpperCase)) {
      this.attackingLand = land
      this.getPlayerActorRef(uuid) match {
        case None =>
        case Some(player) => {
          player._2 ! models.MessageModels.UpdateMap(getMapdata(2, true))
        }
      }
    } else if (this.attackingLand.length != 0 && !gameLogic.getOwnerName(land).equals(uuid.toString.toUpperCase)) {
      val attackerLand = this.attackingLand;
      this.attackingLand = ""
      gameLogic.attack(attackerLand, land)
    }
  }

  def rolledDices() = {
    val attackerDefenderCountries = gameLogic.getAttackerDefenderCountries
    val player1 = players.filter(p => p.uuid.toString().toUpperCase.
      equals(attackerDefenderCountries._1._2.toUpperCase)).headOption match {
      case None => ""
      case Some(player) => player.name
    }
    val player2 = players.filter(p => p.uuid.toString().toUpperCase.
      equals(attackerDefenderCountries._2._2.toUpperCase)).headOption match {
      case None => ""
      case Some(player) => player.name
    }
    val player = (attackerDefenderCountries._1._1 + ", " + player1, attackerDefenderCountries._2._1 + ", " + player2)
    val dices = gameLogic.getRolledDieces
    this.playerActorRefs.foreach(playerActorRef => {
      playerActorRef._2 ! models.MessageModels.RolledDices(player, dices)
    })
  }

  def conqueredACountry() = {
    
  }

  def getMapdata(recoloring: Int = 1, own: Boolean = false): String = {
    val countriesList: scala.collection.mutable.ArrayBuffer[(String, String, Int, Int)] = gameLogic.getCountries
    val countries = countriesList.map(country => {
      val colorHex = Integer.toHexString(gameLogic.getOwnerColor(country._2)).drop(1).drop(1) // drop alpha
      val colorInt = Integer.parseInt(colorHex, 16)
      var recoloringFactor = 1
      if (recoloring != 1) {
        if (own && gameLogic.getCurrentPlayer._1.toUpperCase.equals(country._2.toUpperCase)) {
          recoloringFactor = recoloring
        } else if (!own && !gameLogic.getCurrentPlayer._1.toUpperCase.equals(country._2.toUpperCase)) {
          recoloringFactor = recoloring
        }
      }
      val colorB = (colorInt % 256) / recoloringFactor
      val colorG = ((colorInt / 256) % 256) / recoloringFactor
      val colorR = (((colorInt / 256) / 256) % 256) / recoloringFactor
      (country._1, country._2, country._3, "[" + colorR + ", " + colorG + ", " + colorB + "]")
    })
    val sb: StringBuilder = new StringBuilder

    val newline = "\n"
    sb.append("{" + newline)

    countries.foreach(country => {
      sb.append("\t\"" + country._1 + "\": {")
      sb.append(newline + "\t\t")
      sb.append("\"color\": " + country._4 + "," + newline + "\t\t")
      sb.append("\"troops\": " + country._3 + newline)
      sb.append("\t}," + newline)
    })

    val sbWithoutLast = removeLastChar(sb)

    sbWithoutLast.append("}")

    sbWithoutLast.toString()
  }

  def removeLastChar(stringBuilder: StringBuilder): StringBuilder = {
    val lastIndex = stringBuilder.lastIndexOf(",")
    val splits = stringBuilder.splitAt(lastIndex)
    val inputWithoutChar = splits._1.append(splits._2.drop(1))
    inputWithoutChar
  }
}