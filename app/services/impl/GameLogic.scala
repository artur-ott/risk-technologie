package de.htwg.se.scala_risk.controller.impl

import de.htwg.se.scala_risk.util.Statuses
import de.htwg.se.scala_risk.controller.{ GameLogic => TGameLogic }
import de.htwg.se.scala_risk.model.Continent
import de.htwg.se.scala_risk.model.Country
import de.htwg.se.scala_risk.model.Player
import de.htwg.se.scala_risk.model.World
import de.htwg.se.scala_risk.util.XML
import java.io.File
import java.io.FileOutputStream
import scala.io.Source
import javax.inject.Inject
import javax.inject.Singleton

//@Singleton
class GameLogic /*@Inject()*/ (world: World) extends TGameLogic {

  private[this] var status: Statuses.Value = Statuses.CREATE_GAME
  private[this] val INIT_TROOPS: Int = 3

  private[impl] var attackerDefenderIndex: (Int, Int) = (-1, -1)
  private[impl] var rolledDieces: (List[Int], List[Int]) = (Nil, Nil)
  private var lastState: scala.xml.Node = null
  //private[this] val world: World = new de.htwg.se.scala_risk.model.impl.World // Changed to test GUI

  def startGame = {
    this.setStatus(Statuses.INITIALIZE_PLAYERS)
  }

  def initializeGame() = {
    val players = world.getPlayerList

    if (players.length >= 2) {
      val countries = util.Random.shuffle(world.getCountriesList)

      countries.foreach { x =>
        {
          x.setTroops(INIT_TROOPS)
          x.setOwner(players(countries.indexOf(x) % players.length))
        }
      }
      world.nextPlayer
      this.setStatus(Statuses.GAME_INITIALIZED)
      logic
    } else {
      this.setErrorStatus(Statuses.NOT_ENOUGH_PLAYERS)
    }
  }

  def logic = {
    this.status match {
      case Statuses.GAME_INITIALIZED =>
        checkContinents();
        this.troopsToSpread = world.getPlayerList(world.getCurrentPlayerIndex).getTroops();
        this.setStatus(Statuses.PLAYER_SPREAD_TROOPS)
      case Statuses.PLAYER_SPREAD_TROOPS => this.setStatus(Statuses.PLAYER_ATTACK)
      case Statuses.PLAYER_ATTACK => this.setStatus(Statuses.PLAYER_MOVE_TROOPS)
      case Statuses.PLAYER_MOVE_TROOPS => {
        /// TODO: implement check game
        //      val oldPlayer = world.getCurrentPlayerIndex
        val nextPlayer = world.nextPlayer
        //        if (oldPlayer != -1 && world.getCurrentPlayerIndex == 0) {
        //          this.checkGame
        //        } else {
        //          this.troopsToSpread = nextPlayer.getTroops()
        //          this.setStatus(Statuses.PLAYER_SPREAD_TROOPS)
        //        }
        checkContinents();
        this.troopsToSpread = nextPlayer.getTroops()
        this.setStatus(Statuses.PLAYER_SPREAD_TROOPS)
      }
      case _ =>
    }
  }

  /*
   * TODO: implement check game
  private[this] def checkGame = {
    world.getPlayerList.foreach {
      e => {
        if (world.getCountriesList.exists { x => x.getOwner == e }) {
          
        } else {
          
        }
      }
    }
  }*/

  def endTurn = {
    this.lastState = this.toXml
    this.logic
  }

  private[controller] def setStatus(status: Statuses.Value) = {
    this.status = status
    notifyObservers
  }

  private def setErrorStatus(status: Statuses.Value) = {
    val oldStatus = this.status;
    this.setStatus(status)
    this.setStatus(oldStatus)
  }

  def getStatus: Statuses.Value = this.status

  def getRolledDieces: (List[Int], List[Int]) = this.rolledDieces

  /* Country operations */
  def getCountries: scala.collection.mutable.ArrayBuffer[(String, String, Int, Int)] =
    world.getCountriesList.map { x => (x.getName, x.getOwner.getName, x.getTroops, x.getRefColor()) }

  def getCandidates(country: String = ""): List[(String, String, Int)] = {
    this.status match {
      case Statuses.PLAYER_SPREAD_TROOPS => {
        val list = world.getCountriesList.filter { x => x.getOwner.equals(world.getPlayerList(world.getCurrentPlayerIndex)) }
        list.map { x => (x.getName, x.getOwner.getName, x.getTroops) }.toList
      }
      case Statuses.PLAYER_ATTACK => {
        val candidates = getNeighbours(country).filterNot { x => x.getOwner.equals(world.getPlayerList(world.getCurrentPlayerIndex)) }
        candidates.map { x => (x.getName, x.getOwner.getName, x.getTroops) }
      }
      case Statuses.PLAYER_MOVE_TROOPS => {
        val candidates = getNeighbours(country).filter { x => x.getOwner.equals(world.getPlayerList(world.getCurrentPlayerIndex)) }
        candidates.map { x => (x.getName, x.getOwner.getName, x.getTroops) }
      }
      case _ => Nil
    }
  }

  def getAttackerDefenderCountries: ((String, String, Int, Int), (String, String, Int, Int)) = {
    if (this.attackerDefenderIndex._1 < 0 || this.attackerDefenderIndex._2 < 0) {
      this.setErrorStatus(Statuses.COUNTRY_NOT_FOUND)
      return null
    }

    return (getCountryAsString(this.attackerDefenderIndex._1), getCountryAsString(this.attackerDefenderIndex._2))
  }

  private def getCountryAsString(index: Int): (String, String, Int, Int) = {
    val country: Country = world.getCountriesList(index)
    return (country.getName, country.getOwner.getName, country.getTroops, country.getRefColor())
  }

  private def getNeighbours(country: String): List[Country] = {
    val index = this.getCountryIndexByString(country)
    if (index < 0) this.setErrorStatus(Statuses.COUNTRY_NOT_FOUND); Nil
    world.getCountriesList(index).getNeighboringCountries.toList
  }

  def getAttackerDefenderIndex: (Int, Int) = this.attackerDefenderIndex

  /* Player operations */
  var troopsToSpread = 3//world.getPlayerList(world.getCurrentPlayerIndex).getTroops()

  def getAvailableColors: List[String] = world.getPlayerColorList.map { x => x.toString() }

  def setPlayer(player: (String, String)) = {
    world.addPlayer(player._1, player._2)
    notifyObservers
  }

  def getCurrentPlayer: (String, String) = (
    world.getPlayerList(world.getCurrentPlayerIndex).getName,
    world.getPlayerList(world.getCurrentPlayerIndex).getColor.toString()
  )

  def getTroopsToSpread: Int = this.troopsToSpread

  def addTroops(country: String, troops: Int) = {
    val index = this.getCountryIndexByString(country)
    if (index >= 0) {
      if (world.getCountriesList(index).getOwner.equals(world.getPlayerList(world.getCurrentPlayerIndex))) {
        if (troops <= troopsToSpread) {
          this.lastState = this.toXml
          val countryList = world.getCountriesList.toList
          countryList(index).setTroops(countryList(index).getTroops + troops)
          troopsToSpread -= troops
          if (troopsToSpread == 0) logic
          else notifyObservers
        } else {
          this.setErrorStatus(Statuses.NOT_ENOUGH_TROOPS_TO_SPREAD)
        }
      } else this.setErrorStatus(Statuses.COUNTRY_DOES_NOT_BELONG_TO_PLAYER)
    } else this.setErrorStatus(Statuses.COUNTRY_NOT_FOUND)
  }

  def attack(countryAttacker: String, countryDefender: String) = {
    /* Make check if the player attacks his own country easier */
    if (this.getCurrentPlayer._1.toUpperCase().equals(this.getOwnerName(countryDefender))) {
      this.setErrorStatus(Statuses.PLAYER_ATTACKING_HIS_COUNTRY)
    } else {
      /* Check, if the country to attack is a neighboring country of the
       * attacker country was missing!
       */
      if (!this.getNeighbours(countryAttacker).map { x => x.getName.toUpperCase() }.contains(countryDefender.toUpperCase())) {
        this.setErrorStatus(Statuses.NOT_A_NEIGHBORING_COUNTRY)
      } else {
        if (this.status == Statuses.PLAYER_ATTACK) {
          this.lastState = this.toXml
          this.attackerDefenderIndex = getAttackIndexes(countryAttacker, countryDefender);
          if (attackerDefenderIndex._1 != -1) {
            this.rolledDieces = this.rollDice(
              world.getCountriesList(attackerDefenderIndex._1),
              world.getCountriesList(attackerDefenderIndex._2)
            )
            this.setStatus(Statuses.DIECES_ROLLED)
            val min = Math.min(this.rolledDieces._1.length, this.rolledDieces._2.length)
            var extantTroopsAttacker = world.getCountriesList(attackerDefenderIndex._1).getTroops
            var extantTroopsDefender = world.getCountriesList(attackerDefenderIndex._2).getTroops
            val extant = this.getExtantTroops(extantTroopsAttacker, extantTroopsDefender, min)
            extantTroopsAttacker = extant._1
            extantTroopsDefender = extant._2
            world.getCountriesList(attackerDefenderIndex._1).setTroops(extantTroopsAttacker)
            world.getCountriesList(attackerDefenderIndex._2).setTroops(extantTroopsDefender)
            this.checkConquered(extantTroopsDefender, countryDefender)
          }
        }
      }
    }
  }

  private[impl] def getExtantTroops(troopsAttacker: Int, troopsDefender: Int, min: Int): (Int, Int) = {
    var extantTroopsAttacker = troopsAttacker
    var extantTroopsDefender = troopsDefender
    var i = 0
    for (i <- 0 to min - 1) {
      if (this.rolledDieces._1(i) > this.rolledDieces._2(i)) {
        extantTroopsDefender -= 1
      } else {
        extantTroopsAttacker -= 1
      }
    }
    return (extantTroopsAttacker, extantTroopsDefender)
  }

  private[impl] def checkConquered(extantTroopsDefender: Int, countryDefender: String) = {
    if (extantTroopsDefender == 0) {
      world.getCountriesList(attackerDefenderIndex._2).setOwner(world.getCountriesList(attackerDefenderIndex._1).getOwner)

      if (this.getContinentOwner(countryDefender).toUpperCase().equals(this.getOwnerName(countryDefender).toUpperCase())) {
        this.setStatus(Statuses.PLAYER_CONQUERED_A_CONTINENT)
      } else {
        this.setStatus(Statuses.PLAYER_CONQUERED_A_COUNTRY)
      }
    } else {
      this.clearAttack
      this.setStatus(Statuses.PLAYER_ATTACK)
    }
  }

  private[impl] def getAttackIndexes(countryAttacker: String, countryDefender: String): (Int, Int) = {
    val indexAttacker = this.getCountryIndexByString(countryAttacker)
    val indexDefender = this.getCountryIndexByString(countryDefender)
    if (indexAttacker > -1 && indexDefender > -1) {
      if (world.getCountriesList(indexAttacker).getOwner.equals(world.getPlayerList(world.getCurrentPlayerIndex)) &&
        !world.getCountriesList(indexDefender).getOwner.equals(world.getPlayerList(world.getCurrentPlayerIndex))) {
        return (indexAttacker, indexDefender)
      } else if (world.getCountriesList(indexAttacker).getOwner.equals(world.getPlayerList(world.getCurrentPlayerIndex)) &&
        world.getCountriesList(indexDefender).getOwner.equals(world.getPlayerList(world.getCurrentPlayerIndex))) {
        this.setErrorStatus(Statuses.PLAYER_ATTACKING_HIS_COUNTRY)
      } else if (!world.getCountriesList(indexAttacker).getOwner.equals(world.getPlayerList(world.getCurrentPlayerIndex))) {
        this.setErrorStatus(Statuses.COUNTRY_DOES_NOT_BELONG_TO_PLAYER)
      }
    } else this.setErrorStatus(Statuses.COUNTRY_NOT_FOUND)
    (-1, -1)
  }

  private[this] def clearAttack = {
    this.attackerDefenderIndex = (-1, -1)
    this.rolledDieces = (Nil, Nil)
  }

  def moveTroops(count: Int) = {
    if (this.status == Statuses.PLAYER_CONQUERED_A_COUNTRY || this.status == Statuses.PLAYER_CONQUERED_A_CONTINENT || this.status == Statuses.PLAYER_MOVE_TROOPS) {
      val currentTroops = world.getCountriesList(this.attackerDefenderIndex._1).getTroops
      if (count < 1 || count >= currentTroops)
        this.setErrorStatus(Statuses.INVALID_QUANTITY_OF_TROOPS_TO_MOVE)
      else if (this.status == Statuses.PLAYER_CONQUERED_A_COUNTRY || this.status == Statuses.PLAYER_CONQUERED_A_CONTINENT) {
        world.getCountriesList(this.attackerDefenderIndex._1).setTroops(currentTroops - count)
        world.getCountriesList(this.attackerDefenderIndex._2).setTroops(count)
        this.clearAttack
        this.setStatus(Statuses.PLAYER_ATTACK)
      } else {
        this.lastState = this.toXml
        world.getCountriesList(this.attackerDefenderIndex._1).setTroops(currentTroops - count)
        world.getCountriesList(this.attackerDefenderIndex._2).setTroops(world.getCountriesList(this.attackerDefenderIndex._2).getTroops + count)
        this.clearAttack
        this.setStatus(Statuses.PLAYER_MOVE_TROOPS)
      }
    }
  }

  private[this] def getCountryIndexByString(country: String): Int = world.getCountriesList.indexWhere { x => x.getName.toUpperCase().equals(country.toUpperCase()) }

  def dragTroops(countryFrom: String, countryTo: String, troops: Int) = {
    if (this.status == Statuses.PLAYER_MOVE_TROOPS) {
      this.attackerDefenderIndex = (
        this.getCountryIndexByString(countryFrom),
        this.getCountryIndexByString(countryTo)
      )
      if (this.attackerDefenderIndex._1 < 0 || this.attackerDefenderIndex._2 < 0) {
        this.clearAttack
        this.setErrorStatus(Statuses.COUNTRY_NOT_FOUND)
      } else {
        if (!this.getNeighbours(countryFrom).map { x => x.getName.toUpperCase() }.contains(countryFrom)) {
          this.setErrorStatus(Statuses.NOT_A_NEIGHBORING_COUNTRY)
        } else {
          this.moveTroops(troops)
        }
        
      }
    }
  }

  // Function to attack a country. TCountry is the trait type!
  def rollDice(attacker: Country, defender: Country): (List[Int], List[Int]) = {
    val troopsAttacker = attacker.getTroops
    val toopsDefender = defender.getTroops
    var dicesAttacker: List[Int] = Nil
    var dicesDefender: List[Int] = Nil
    //return (6 :: 6 :: 6 :: Nil, 5 :: 5 :: 5 :: Nil) // TODO: for testing remove comment 
    if (troopsAttacker > 1) {
      troopsAttacker match {
        case 2 => dicesAttacker = List.fill(1)(randomDice())
        case 3 => dicesAttacker = List.fill(2)(randomDice())
        case _ => dicesAttacker = List.fill(3)(randomDice())
      }
      toopsDefender match {
        case 1 => dicesDefender = List.fill(1)(randomDice())
        case _ => dicesDefender = List.fill(2)(randomDice())
        //case _ => dicesDefender = List.fill(3)(randomDice())
      }
      return (dicesAttacker.sortWith(_ > _), dicesDefender.sortWith(_ > _))
    } else {
      this.setErrorStatus(Statuses.NOT_ENOUGH_TROOPS_TO_ATTACK)
      return (Nil, Nil)
    }
  }
  // Function to get dice values from 1 to 6
  def randomDice(): Int = ((Math.random() * 6) + 1).toInt

  def getCurrentPlayerColor(): String = {
    return world.getPlayerList(world.getCurrentPlayerIndex).getColor.toString()
  }

  def getOwnerColor(owner: String): Int = {
    val playerList = world.getPlayerList
    var intcolor = 0
    var color = ""
    playerList.foreach { x => if (x.getName.toUpperCase().equals(owner.toUpperCase())) { color = x.getColor.toString().toUpperCase() } }

    color match {
      case "RED" => intcolor = -57312
      case "YELLOW" => intcolor = -5888
      case "GREEN" => intcolor = -10420362
      case "BLUE" => intcolor = -8350209
      case "PINK" => intcolor = -563473
      case "ORANGE" => intcolor = -355265
      case _ => intcolor = 0
    }
    return intcolor
  }

  def getOwnerName(country: String): String = {
    val countryList = world.getCountriesList
    var ownerName = ""
    countryList.foreach { x => if (x.getName.toUpperCase().equals(country.toUpperCase())) { ownerName = x.getOwner.getName.toUpperCase() } }
    return ownerName
  }

  def checkContinents() {
    val playerList = world.getPlayerList
    val continentList = world.getContinentList

    playerList.foreach { x => x.setTroops(this.INIT_TROOPS); continentList.foreach { c => if (c.getOwner() == x) { x.setTroops(x.getTroops() + c.getBonusTroops()) } } }

  }
  
  def getContinentOwner(countryName: String): String = {
    val continentList = world.getContinentList
    var continentName = ""
    continentList.foreach { x => if (x.getIncludedCountries().map { y => y.getName }.contains(countryName)) { continentName = x.getOwner().getName } }
    return continentName
  }

  def saveGame = {
    val file: File = new File("./save/savegame.xml")
    val fos: FileOutputStream = new FileOutputStream(file, false)
    val saveXML: Array[Byte] = this.toXml.toString().getBytes
    fos.write(saveXML)
    fos.close()
  }

  def loadGame = {
    val filename = "./save/savegame.xml"
    //this.fromXml(scala.xml.XML.loadFile(filename))
    this.fromXml(scala.xml.XML.load(new java.io.InputStreamReader(new java.io.FileInputStream(filename), "UTF-8")))
  }

  def toXml: scala.xml.Node = {
    <GameLogic>
      <status>{ this.status.toString() }</status>
      <attackerDefenderIndex>{ this.getAttackerDefenderIndexXml(this.attackerDefenderIndex) }</attackerDefenderIndex>
      <rolledDieces>{ this.getRolledDiecesXml(this.rolledDieces) }</rolledDieces>
      <troopsToSpread>{ this.troopsToSpread }</troopsToSpread>
      <world>{ this.world.toXml }</world>
    </GameLogic>
  }

  def getRolledDiecesXml(node: (List[Int], List[Int])): scala.xml.Elem = {
    val xml = <tuple></tuple>
    var firstList = <list id="first"></list>
    var secondList = <list id="second"></list>
    node._1.foreach(x => {
      val xmlEl = <listEl>{ x }</listEl>
      //firstList = XML.addXmlChild(firstList, xmlEl)
    })
    node._2.foreach(x => {
      val xmlEl = <listEl>{ x }</listEl>
      //secondList = XML.addXmlChild(secondList, xmlEl)
    })
    return <elem></elem> //XML.addXmlChild(XML.addXmlChild(xml, firstList), secondList)
  }

  def getAttackerDefenderIndexXml(node: (Int, Int)): scala.xml.Elem = {
    val xml = <tuple></tuple>
    val first = <element id="first">{ node._1 }</element>
    val second = <element id="second">{ node._2 }</element>
    return <tuple></tuple> //XML.addXmlChild(XML.addXmlChild(xml, first), second)
  }

  def fromXml(node: scala.xml.Node) = {
    // get status
    this.status = Statuses.withName((node \ "status").text.toUpperCase())
    // get attackerDefenderIndex
    this.attackerDefenderIndex = ((node \ "attackerDefenderIndex")(0).child.head.child.head.text.toInt, (node \ "attackerDefenderIndex")(0).child.head.child.last.text.toInt)
    // get rolledDieces
    var first = List[Int]()
    var second = List[Int]()
    (node \ "rolledDieces")(0).child.head.child.head.child.foreach(x => first = x.text.toInt :: first)
    (node \ "rolledDieces")(0).child.head.child.last.child.foreach(x => second = x.text.toInt :: second)
    this.rolledDieces = (first.sortWith(_ > _), second.sortWith(_ > _))
    // get troops to spreed
    this.troopsToSpread = (node \ "troopsToSpread").text.toInt
    this.world.fromXml((node \ "world")(0))
    this.notifyObservers
  }

  def undo = {
    if (this.lastState != null) {
      val temp = this.lastState
      this.lastState = null
      this.fromXml(temp)
    }
  }

}