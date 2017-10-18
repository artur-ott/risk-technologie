package de.htwg.se.scala_risk.controller

trait CountryController {
  def getCountries: scala.collection.mutable.ArrayBuffer[(String, String, Int, Int)]
}