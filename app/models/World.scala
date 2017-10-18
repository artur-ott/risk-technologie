package de.htwg.se.scala_risk.model

import scala.collection.mutable.ArrayBuffer
import de.htwg.se.scala_risk.model.impl.Colors._

trait World {
  def getCountriesList: ArrayBuffer[Country]

  def getPlayerList: ArrayBuffer[Player]
  def getDefaultPlayer: Player
  def nextPlayer: Player
  def getCurrentPlayerIndex: Int
  def addPlayer(name: String, color: String)
  def getPlayerColorList: List[Color]

  def getContinentList: ArrayBuffer[Continent]

  def toXml: scala.xml.Elem
  def fromXml(node: scala.xml.Node)

}