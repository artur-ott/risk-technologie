package de.htwg.se.scala_risk.controller

import de.htwg.se.scala_risk.util.observer.Obserable
import de.htwg.se.scala_risk.util.Statuses
import de.htwg.se.scala_risk.model.Country

trait GameLogic extends Obserable with PlayerController with CountryController {
  def getCountries: scala.collection.mutable.ArrayBuffer[(String, String, Int, Int)]
  def startGame
  def initializeGame
  def getStatus: Statuses.Value
  def getRolledDieces: (List[Int], List[Int])
  def endTurn
  def getAttackerDefenderCountries: ((String, String, Int, Int), (String, String, Int, Int))
  def moveTroops(count: Int)
  def getCandidates(country: String = ""): List[(String, String, Int)]
  def dragTroops(countryFrom: String, countryTo: String, troops: Int)
  def attack(countryAttacker: String, countryDefender: String)
  def getCurrentPlayerColor(): String

  //def setStatus(status: Statuses.Value)// TODO: REMOVE setStatus

  def getOwnerColor(owner: String): Int
  def getOwnerName(country: String): String

  def saveGame
  def loadGame
  def undo
}
