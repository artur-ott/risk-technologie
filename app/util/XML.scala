package de.htwg.se.scala_risk.util

object XML {
  def addXmlChild(n: scala.xml.Node, newChild: scala.xml.Node) = n match {
    case scala.xml.Elem(prefix, label, attribs, scope, child @ _*) =>
      scala.xml.Elem(prefix, label, attribs, scope, child ++ newChild: _*)
    case _ => "" //error("Can only add children to elements!")
  }
}