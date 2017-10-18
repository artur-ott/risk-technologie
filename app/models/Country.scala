package de.htwg.se.scala_risk.model
/**
 * Trait country is the interface for countries.
 * @author Nico Lutz
 */
trait Country {
  /**
   * Returns the name of the Country.
   * @return Name of the country.
   */
  def getName: String

  /**
   * Returns the set of neighboring countries of a country.
   * @return Neighboring countries.
   */
  def getNeighboringCountries: Set[Country]

  /**
   * Returns the amount of troops stationed in a country.
   * @return Number of troops.
   */
  def getTroops: Int

  /**
   * Sets the troops of a country to a specific value
   * e.g. when reinfocing or when a player loses troops.
   * @param number new amount of troops of the country.
   */
  def setTroops(number: Int)

  /**
   * Returns the player owning the country.
   * @return Owner.
   */
  def getOwner: Player

  /**
   * Sets the owner of the country to the argument
   * e.g. when a player conquers a country or
   * when initializing the game.
   */
  def setOwner(player: Player)

  /**
   * Returns the unique color of the country.
   * @return color as integer.
   */
  def getRefColor(): Int

  def setNeighboringCountries(neighbor: Set[Country])

  def toXml: scala.xml.Elem
}
