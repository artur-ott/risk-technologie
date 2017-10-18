package de.htwg.se.scala_risk.model

import scala.collection._
import scala.collection.mutable.ArrayBuffer
/**
 * Trait Continent is the interface for Continents.
 * @author Nico Lutz
 */
trait Continent {
  /**
   * Returns the owner of the continent, default player if
   * no one owns it.
   * @return Owner of the continent.
   */
  def getOwner(): Player

  /**
   * Returns the name of the continent.
   * @return Name of the continent.
   */
  def getName(): String

  /**
   * Returns the amount of troops a player gets additionaly
   * when he owns this continent.
   * @return bonus troops.
   */
  def getBonusTroops(): Int

  /**
   * Returns all the countries in this continent.
   * @return Countries of this continent.
   */
  def getIncludedCountries(): ArrayBuffer[Country]

  def toXml: scala.xml.Elem
}