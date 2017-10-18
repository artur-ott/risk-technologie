package de.htwg.se.scala_risk.model
import de.htwg.se.scala_risk.model.impl.Colors._
import scala.collection._
/**
 * Trait Player is the interface for players.
 * @author Nico Lutz
 */
trait Player {
  /**
   * Returns the name of the player.
   * @return Name of the player.
   */
  def getName: String

  /**
   * Returns the color of the player.
   * @return Color of the player.
   */
  def getColor: Color

  /**
   * Returns all countries the player owns as a Set.
   * @return Set of countries.
   */
  def getOwnedCountries: immutable.Set[Country]

  def equals(that: Any): Boolean

  def getTroops(): Int

  def setTroops(troops: Int)

  def toXml: scala.xml.Elem
}