package de.htwg.se.scala_risk.model.impl
import de.htwg.se.scala_risk.model.{ Country => TCountry }
import de.htwg.se.scala_risk.model.{ Player => TPlayer }
import de.htwg.se.scala_risk.model.{ Continent => TContinent }
import de.htwg.se.scala_risk.model.{ World => TWorld }
import scala.collection._
import scala.collection.mutable.ArrayBuffer

/**
 * Class to create continents.
 * @author Nico Lutz
 */

case class Continent(name: String, countries: ArrayBuffer[TCountry],
    bonusTroops: Int, world: TWorld) extends de.htwg.se.scala_risk.model.Continent {

  override def getOwner(): TPlayer = {
    val ownerCandidate: TPlayer = this.getIncludedCountries.head.getOwner
    val allOwnedByOne: Boolean = this.getIncludedCountries.forall { x => x.getOwner == ownerCandidate }
    if (allOwnedByOne) ownerCandidate
    else world.getDefaultPlayer
  }

  override def getIncludedCountries(): ArrayBuffer[TCountry] = {
    //    var includedCountries: immutable.Set[TCountry] = immutable.Set()
    //    for (x <- this.countries) includedCountries += world.getCountriesList(x)
    //    return includedCountries
    return this.countries
  }
  override def getName(): String = return this.name
  override def getBonusTroops(): Int = return this.bonusTroops
  override def equals(that: Any): Boolean = {
    if (!that.isInstanceOf[TContinent]) return false
    var p = that.asInstanceOf[TContinent]
    if (this.name.equals(p.getName)) return true else return false
  }
  override def toString(): String = {
    var containingCountries = ""
    this.getIncludedCountries().foreach { x => containingCountries += x.getName + " " }
    "Name: " + name + ", containing countries: " + containingCountries + ", bonus troops: " + bonusTroops + ", owner: " + this.getOwner()
  }

  def toXml: scala.xml.Elem = {
    /*var xml: scala.xml.Elem = <continent></continent>
    import de.htwg.se.scala_risk.util.XML
    xml = XML.addXmlChild(xml, <name>{ this.name }</name>)
    xml = XML.addXmlChild(xml, <bonusTroops>{ this.bonusTroops }</bonusTroops>)
    countries.foreach { x => xml = XML.addXmlChild(xml, <country>{ x.getName.toString() }</country>) }
    xml*/
    <continent></continent>
  }
}