package de.htwg.se.scala_risk.util.observer

class Obserable {
  var subscribers: Vector[TObserver] = Vector()
  def add(s: TObserver) = subscribers = subscribers :+ s
  def remove(s: TObserver) = subscribers = subscribers.filterNot(x => x == s)
  def notifyObservers = subscribers.foreach(x => x.update())
}