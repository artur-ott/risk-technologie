package controllers

import javax.inject._

import play.api.mvc._

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
class GameController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an action that responds with the [[Counter]]'s current
   * count. The result is plain text. This `Action` is mapped to
   * `GET /count` requests by an entry in the `routes` config file.
   */

  def index = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index(GamesShared.getGames))
  }

  def start = Action { implicit request: Request[AnyContent] =>
    request.body.asFormUrlEncoded match {
      case None => BadRequest("")
      case Some(post) => {
        if (post.contains("game_selected")) {
          if (GamesShared.getGames.filter(_.id.equals(post.get("game_selected").mkString(""))).length == 0) {
            Redirect(routes.GameController.index(), 302)
          } else {
            val gameModel = GamesShared.getGames.filter(_.id.equals(post.get("game_selected").mkString(""))).head
            gameModel.player += PlayerModel(post.get("player_name").mkString(""))

            println(GamesShared.getGames.toString)
            println(post.get("player_name").mkString("") + " ist dem Spiel "
              + post.get("game_selected").mkString("") + " beigetreten")
            Redirect(routes.GameController.game(), 302).withSession("user" -> post.get("player_name").mkString(""))
          }
        } else {

        }
      }
    }
    Ok("Test")
    /*if (request.body.asFormUrlEncoded.get.contains("game_selected")) {
      if (GamesShared.getGames.filter(_.id.equals(request.body.asFormUrlEncoded.get("game_selected").head.toString)).length == 0) {
        Redirect(routes.GameController.index(), 302)
      } else {
        val gameModel = GamesShared.getGames.filter(_.id.equals(request.body.asFormUrlEncoded.get("game_selected").head.toString)).head
        gameModel.player += PlayerModel(request.body.asFormUrlEncoded.get("player_name").head.toString)

        println(GamesShared.getGames.toString)
        println(request.body.asFormUrlEncoded.get("player_name").head.toString + " ist dem Spiel "
          + request.body.asFormUrlEncoded.get("game_selected").head.toString + " beigetreten")
        Redirect(routes.GameController.game(), 302).withSession("user" -> request.body.asFormUrlEncoded.get("player_name").head.toString)
      }
    } else {
      val playerList = ListBuffer[PlayerModel]()
      playerList += PlayerModel(request.body.asFormUrlEncoded.get("player_name").head.toString)
      GamesShared.addGame(GameModel("mein Spiel", playerList, None))
      println(request.body.asFormUrlEncoded.get("player_name").head.toString + " hat ein Spiel gestartet")
      Redirect(routes.GameController.game(), 302).withSession("user" -> request.body.asFormUrlEncoded.get("player_name").head.toString)
    }*/
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
}
