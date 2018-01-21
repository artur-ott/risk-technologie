package services.game

import java.util.UUID

import scala.collection.mutable._
import akka.actor._

import models.PlayerModel

import de.htwg.se.scala_risk.util.observer.TObserver
import de.htwg.se.scala_risk.util.Statuses
import de.htwg.se.scala_risk.controller.GameLogic

class GameManager(gameLogic: GameLogic, var players: ListBuffer[PlayerModel] = ListBuffer()) extends Actor with TObserver {
  val playerActorRefs: ListBuffer[(UUID, ActorRef)] = ListBuffer()
  var actionLand: String = ""
  var gameStarted: Boolean = false
  gameLogic.add(this)

  def startedGame: Boolean = this.gameStarted;

  def receive = {
    case models.MessageModels.SetPlayer(prop, uuid) => this.createPlayer(prop, uuid)
    case models.MessageModels.StartGame(uuid) => this.startGame(uuid)
    case models.MessageModels.ClickedLand(uuid, land) => this.clickedLand(uuid, land)
    case models.MessageModels.MoveTroops(uuid, troops) => this.moveTroops(uuid, troops)
    case models.MessageModels.EndTurn(uuid) => this.endTurn(uuid)
    case models.MessageModels.ResetTransfereTroops(uuid) => if (uuid.toString().toUpperCase.equals(gameLogic.getCurrentPlayer._1.toUpperCase)) this.actionLand = ""
    case models.MessageModels.DragTroops(uuid, landFrom, landTo, troops) => this.dragTroops(uuid, (landFrom, landTo), troops)
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
      case Statuses.PLAYER_CONQUERED_A_CONTINENT => this.conqueredACountry
      case Statuses.PLAYER_MOVE_TROOPS => this.transfereTroops

      case Statuses.NOT_ENOUGH_PLAYERS => {
        println("NOT_ENOUGH_PLAYERS: " + players.size)
      }

      case _ => println("Update: " + gameLogic.getStatus)
    }
  }

  def createPlayer(playerRef: ActorRef, uuid: UUID) = {
    this.getPlayerActorRef(uuid) match {
      case None => playerActorRefs += ((uuid, playerRef))
      case Some(child) => {
        context.stop(child._2)
        playerActorRefs - child
        playerActorRefs += ((uuid, playerRef))
      }
    }
    val playersNameColor = players.map { player => (player.name, player.color) }
    playerActorRefs.foreach(player => {
      player._2 ! models.MessageModels.PlayerList(playersNameColor.toList)
    })
  }

  def currentPlayerName: String = {
    players.filter(p => p.uuid.toString().toUpperCase.equals(gameLogic.getCurrentPlayer._1.toUpperCase)).headOption match {
      case None => ""
      case Some(player) => player.name
    }
  }

  def startGame(uuid: UUID) = {
    if (!this.players.map { player =>
      if (player.uuid.toString().toUpperCase.equals(uuid.toString().toUpperCase)) {
        player.startedGame = true;
      }
      player.startedGame
    }.contains(false) && this.playerActorRefs.size > 1) {
      this.gameStarted = true
      gameLogic.startGame
    }
  }

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
          case Some(player) =>
            player.color = color
            gameLogic.setPlayer((player.uuid.toString, color))
        }
      }
    }
  }

  def gameInitialized() = {
    val playersNameColor = players.map { player => (player.name, player.color) }
    this.playerActorRefs.foreach(playerActorRef => {
      playerActorRef._2 ! models.MessageModels.UpdateMap(getMapdata())
      playerActorRef._2 ! models.MessageModels.PlayerList(playersNameColor.toList)
    })
  }

  def clickedLand(uuid: UUID, land: String) {
    if (gameLogic.getStatus != Statuses.CREATE_GAME) {
      if (uuid.toString().toUpperCase.equals(gameLogic.getCurrentPlayer._1.toUpperCase)) {
        gameLogic.getStatus match {
          case Statuses.PLAYER_SPREAD_TROOPS => gameLogic.addTroops(land, 1)
          case Statuses.PLAYER_ATTACK => this.attack(uuid, land)
          case Statuses.PLAYER_MOVE_TROOPS => this.selectMoveTroop(uuid, land)
          case _ => println("Land (" + land + ") clicked by (" + uuid + ") in wrong status(" + gameLogic.getStatus + ")")
        }
      }
    }
  }

  def spreadTroops() = {
    val troops = gameLogic.getTroopsToSpread
    this.playerActorRefs.foreach(playerActorRef => {
      playerActorRef._2 ! models.MessageModels.SpreadTroops(currentPlayerName, troops)
      playerActorRef._2 ! models.MessageModels.UpdateMap(getMapdata(if (playerActorRef._1.toString.toUpperCase.equals(gameLogic.getCurrentPlayer._1.toUpperCase)) 2 else 1))
    })
  }

  def playerAttacking() = {
    this.playerActorRefs.foreach(playerActorRef => {
      playerActorRef._2 ! models.MessageModels.PlayerAttack(currentPlayerName)
      playerActorRef._2 ! models.MessageModels.UpdateMap(getMapdata(if (playerActorRef._1.toString.toUpperCase.equals(gameLogic.getCurrentPlayer._1.toUpperCase)) 2 else 1))
    })
  }

  def attack(uuid: UUID, land: String) = {
    if (this.actionLand.length == 0 && gameLogic.getOwnerName(land).equals(uuid.toString.toUpperCase)) {
      this.actionLand = land
      this.getPlayerActorRef(uuid) match {
        case None =>
        case Some(player) => {
          player._2 ! models.MessageModels.UpdateMap(getMapdata(2, true))
        }
      }
    }
    if (this.actionLand.length != 0 && !gameLogic.getOwnerName(land).equals(uuid.toString.toUpperCase)) {
      val attackerLand = this.actionLand;
      this.actionLand = ""
      gameLogic.attack(attackerLand, land)
    }
  }

  def rolledDices() = {
    val attackerDefenderCountries = gameLogic.getAttackerDefenderCountries
    val player = (attackerDefenderCountries._1._1.toString, attackerDefenderCountries._2._1.toString)
    val dices = gameLogic.getRolledDieces
    this.playerActorRefs.foreach(playerActorRef => {
      playerActorRef._2 ! models.MessageModels.RolledDices(player, dices)
    })
  }

  def conqueredACountry() = {
    val attackerDefenderCountries = gameLogic.getAttackerDefenderCountries
    this.playerActorRefs.filter(p => p._1.toString().toUpperCase.
      equals(attackerDefenderCountries._1._2.toUpperCase)).foreach(
      p => p._2 ! models.MessageModels.PlayerConqueredCountry(attackerDefenderCountries._1._3)
    )
    this.playerActorRefs.filter(p => !p._1.toString().toUpperCase.
      equals(attackerDefenderCountries._1._2.toUpperCase)).foreach(
      p => p._2 ! models.MessageModels.ConqueredCountry(attackerDefenderCountries._2._1)
    )
  }

  def moveTroops(uuid: UUID, troops: Int) = {
    if (uuid.toString().toUpperCase.equals(gameLogic.getCurrentPlayer._1.toUpperCase)) {
      gameLogic.getStatus match {
        case Statuses.PLAYER_CONQUERED_A_COUNTRY => gameLogic.moveTroops(troops)
        case Statuses.PLAYER_CONQUERED_A_CONTINENT => gameLogic.moveTroops(troops)
        case _ => println("Move trrops (" + troops + ") by (" + uuid + ") in wrong status(" + gameLogic.getStatus + ")")
      }
    }
  }

  def endTurn(uuid: UUID) = {
    if (uuid.toString().toUpperCase.equals(gameLogic.getCurrentPlayer._1.toUpperCase)) {
      gameLogic.getStatus match {
        case Statuses.PLAYER_ATTACK => gameLogic.endTurn
        case Statuses.PLAYER_MOVE_TROOPS => gameLogic.endTurn
        case _ =>
      }
    }
  }

  def transfereTroops() = {
    this.playerActorRefs.foreach(playerActorRef => {
      playerActorRef._2 ! models.MessageModels.TransfereTroops(playerActorRef._1.toString().toUpperCase.equals(gameLogic.getCurrentPlayer._1.toUpperCase))
    })
  }

  def selectMoveTroop(uuid: UUID, land: String) = {
    if (uuid.toString().toUpperCase.equals(gameLogic.getCurrentPlayer._1.toUpperCase) && gameLogic.getStatus == Statuses.PLAYER_MOVE_TROOPS) {
      gameLogic.getCountries.foreach(landObject => {
        if (landObject._1.equals(land) && uuid.toString().toUpperCase.equals(landObject._2.toUpperCase)) {
          if (this.actionLand.length > 0) {
            if (!this.actionLand.equals(land)) {
              this.playerActorRefs.filter(p => p._1.toString().toUpperCase.equals(uuid.toString().toUpperCase)).foreach(
                p => p._2 ! models.MessageModels.TransfereTroopsSetToLand(land)
              )
            }
          } else {
            this.actionLand = land
            this.playerActorRefs.filter(p => p._1.toString().toUpperCase.equals(uuid.toString().toUpperCase)).foreach(
              p => p._2 ! models.MessageModels.TransfereTroopsSetFromLand(this.actionLand, landObject._3)
            )
          }
        }
      })
    }
  }

  def dragTroops(uuid: UUID, lands: (String, String), troops: Int) = {
    if (uuid.toString().toUpperCase.equals(gameLogic.getCurrentPlayer._1.toUpperCase) && gameLogic.getStatus == Statuses.PLAYER_MOVE_TROOPS) {
      gameLogic.dragTroops(lands._1, lands._2, troops)
      this.playerActorRefs.foreach(playerActorRef => {
        playerActorRef._2 ! models.MessageModels.UpdateMap(getMapdata(if (playerActorRef._1.toString.toUpperCase.equals(uuid.toString().toUpperCase)) 2 else 1))
      })
      this.actionLand = ""
    }
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
        }
        if (!own && !gameLogic.getCurrentPlayer._1.toUpperCase.equals(country._2.toUpperCase)) {
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