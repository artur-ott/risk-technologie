package controllers

import javax.inject._
import play.api.mvc._

import play.api.libs.streams.ActorFlow
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.actor._
import scala.concurrent.Future

import services.game.{SocketActor => GameSocketActor}
import de.htwg.se.scala_risk.model.impl._
import scala.collection.mutable._
import models._
import models.shared.GamesShared

/**
 * This controller demonstrates how to use dependency injection to
 * bind a component into a controller class. The class creates an
 * `Action` that shows an incrementing count to users. The [[Counter]]
 * object is injected by the Guice dependency injection system.
 */
@Singleton
class GameController @Inject() (cc: ControllerComponents) (implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  /**
   * Create an action that responds with the [[Counter]]'s current
   * count. The result is plain text. This `Action` is mapped to
   * `GET /count` requests by an entry in the `routes` config file.
   */
  val test: Array[String] = Array(
    "SDFGHJKLOELKJHGFFG",
    "WERTZUIOPPPPPOIZTD"
  )

  def index = Action {
    Ok(views.html.index()).withSession("user" -> this.test(0))
  }

  def start = Action { implicit request: Request[AnyContent] =>
    request.body.asFormUrlEncoded match {
      // No request
      case None => BadRequest("")
      case Some(post) => {
        if (post.contains("game_selected")) {
          val gameSelected = post.get("game_selected").getOrElse(List[String]("-1")).mkString;
          if (!GamesShared.getGames.exists(_.id.equals(gameSelected))) {
            // No game found
            Redirect(routes.GameController.index(), 302)
          } else {
            GamesShared.getGames.filter(_.id.equals(gameSelected)).headOption match {
              // No game found
              case None => BadRequest("")
              case Some(gameModel) => {

                post.get("player_name") match {
                  // No player found
                  case None => BadRequest("")
                  case Some(player) => {
                    gameModel.player += PlayerModel(player.mkString)

                    println(GamesShared.getGames.toString)
                    println(player.mkString + " ist dem Spiel "
                      + gameSelected + " beigetreten")

                    Redirect(routes.GameController.game(), 302).withSession("user" -> player.mkString)
                  }
                }
              }
            }
          }
        } else {
          post.get("player_name") match {
            // No player found
            case None => BadRequest("")
            case Some(player) => {
              val playerList = ListBuffer[PlayerModel]()
              playerList += PlayerModel(player.mkString)
              GamesShared.addGame(GameModel("mein Spiel", playerList, None))

              println(player.mkString + " hat ein Spiel gestartet")

              Redirect(routes.GameController.game(), 302).withSession("user" -> player.mkString)
            }
          }
        }
      }
    }
  }

  def game = Action {
    Ok(views.html.general.game())
  }

  def description = Action {
    Ok(views.html.game.description())
  }

  def rules = Action {
    Ok(views.html.game.rules())
  }

  def socket = WebSocket.acceptOrResult[String, String] { request =>
    Future.successful(request.session.get("user") match {
      case None => Left(Forbidden)
      case Some(user) => Right(ActorFlow.actorRef { out =>
        println("Connect received from: " + user)
        Props(new GameSocketActor(out))
      })
    })
  }
}
