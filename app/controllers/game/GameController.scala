package controllers.game

import controllers._

import javax.inject._
import play.api.mvc._
import org.webjars.play.WebJarsUtil
import play.api.i18n.{ I18nSupport, Messages }
import utils.auth.DefaultEnv

import play.api.libs.streams.ActorFlow
import akka.stream.Materializer
import akka.actor._
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.{ LogoutEvent, Silhouette }
import scala.concurrent.Future
import scala.collection.mutable.ListBuffer

import de.htwg.se.scala_risk.model.impl.{ World => ImplWorld }
import de.htwg.se.scala_risk.controller.impl.{ GameLogic => ImplGameLogic }
import services.game.{ SocketActor => GameSocketActor }
import services.game.GameManager
import models.shared.GamesShared
import models._

/**
 */
@Singleton
class GameController @Inject() (cc: ControllerComponents, silhouette: Silhouette[DefaultEnv])(implicit system: ActorSystem, mat: Materializer, webJarsUtil: WebJarsUtil, assets: AssetsFinder) extends AbstractController(cc) with I18nSupport {

  val actorSystem = ActorSystem("Risiko")

  /**
   * Create an action that responds with the [[Counter]]'s current
   * count. The result is plain text. This `Action` is mapped to
   * `GET /count` requests by an entry in the `routes` config file.
   */

  def index = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.game.index(request.identity, GamesShared.getGames)))
  }

  def start = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    request.body.asFormUrlEncoded match {
      // No request
      case None => Future.successful(BadRequest(""))
      case Some(post) => {
        if (post.contains("game_selected")) {
          selectedGame(post.get("game_selected").getOrElse(List[String]("-1")).mkString)
        } else {
          if (post.contains("game_name")) {
            selectedGame(post.get("game_name").getOrElse(List[String]("-1")).mkString)
          } else {
            Future.successful(BadRequest(""))
          }
        }
      }
    }
  }

  private[this] def createGame(gameName: String): Future[Result] = {
    if (gameName.equals("-1") || gameName.length == 0) Future.successful(BadRequest(""))
    else {
      val playerList = ListBuffer[PlayerModel]()
      playerList += PlayerModel("player.mkString")
      val world = new ImplWorld()
      val gameName = "mein Spiel"
      GamesShared.addGame(GameModel(gameName, playerList,
        Some(actorSystem.actorOf(Props(new GameManager(new ImplGameLogic(world), playerList)), name = gameName.replaceAll("\\s", "")))
      ))

      println("player.mkString" + " hat ein Spiel gestartet")

      // TODO: kommentare entfernen
      //Redirect(routes.game.GameController.game(), 302).withSession("user" -> player.mkString)
      Future.successful(BadRequest(""))
    }
  }

  private[this] def selectedGame(gameSelected: String): Future[Result] = {
    if (!GamesShared.getGames.exists(_.id.equals(gameSelected))) {
      // No game found
      // TODO: kommentare entfernen
      //Redirect(routes.game.GameController.index(), 302)
      Future.successful(BadRequest(""))
    } else {
      GamesShared.getGames.filter(_.id.equals(gameSelected)).headOption match {
        // No game found
        case None => Future.successful(BadRequest(""))
        case Some(gameModel) => {

          // TODO: add player
          /*post.get("player_name") match {
            // No player found
            case None => Future.successful(BadRequest(""))
            case Some(player) => {
              gameModel.player += PlayerModel(player.mkString)

              println(GamesShared.getGames.toString)
              println(player.mkString + " ist dem Spiel "
                + gameSelected + " beigetreten")
              // TODO: kommentare entfernen
              //Redirect(routes.game.GameController.game(), 302).withSession("user" -> player.mkString)
              Future.successful(BadRequest(""))
            }
          }*/
          Future.successful(BadRequest(""))
        }
      }
    }
  }

  def game = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.game.game()))
  }

  def description = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.game.description()))
  }

  def rules = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.game.rules()))
  }

  def socket = WebSocket.acceptOrResult[String, String] { request =>
    Future.successful(request.session.get("user") match {
      case None => Left(Forbidden)
      case Some(user) => Right(ActorFlow.actorRef { out =>
        GamesShared.getGameWithPlayer(user) match {
          case None => Props.empty
          case Some(game) => {
            println("Received a socket connection from Player: " + user)
            game.gameManager match {
              case None => Props.empty
              case Some(gameManager) => {
                println("Socket connection created: " + user)
                Props(new GameSocketActor(out, gameManager, user))
              }
            }
          }
        }
      })
    })
  }
}
