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
import com.mohiva.play.silhouette.api.{ LogoutEvent, HandlerResult, Silhouette }
import scala.concurrent.Future
import scala.collection.mutable.ListBuffer

import de.htwg.se.scala_risk.model.impl.{ World => ImplWorld }
import de.htwg.se.scala_risk.controller.impl.{ GameLogic => ImplGameLogic }
import services.game.{ SocketActor => GameSocketActor, GameManager }
import models.shared.GamesShared
import models._
import scala.concurrent.ExecutionContext.Implicits.global

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
    Future.successful(
      request.body.asFormUrlEncoded match {
        // No request
        case None => BadRequest("")
        case Some(post) => {
          if (post.contains("game_selected")) {
            selectedGame(request.identity, post.get("game_selected").getOrElse(List[String]("-1")).mkString)
          } else {
            if (post.contains("game_name")) {
              createGame(request.identity, post.get("game_name").getOrElse(List[String]("-1")).mkString)
            } else {
              BadRequest("")
            }
          }
        }
      }
    )
  }

  private[this] def createGame(user: User, gameName: String): Result = {
    if (gameName.equals("-1") || gameName.length == 0) BadRequest("")
    else {
      val playerList = ListBuffer[PlayerModel]()
      playerList += PlayerModel(user.userID, user.email.getOrElse(""))
      val world = new ImplWorld()
      GamesShared.addGame(GameModel(gameName, playerList,
        Some(actorSystem.actorOf(Props(new GameManager(new ImplGameLogic(world), playerList)), name = gameName.replaceAll("\\s", "")))
      ))

      println(user.email.getOrElse("") + " hat ein Spiel gestartet")

      Redirect(routes.GameController.game(), 302)
    }
  }

  private[this] def selectedGame(user: User, gameSelected: String): Result = {
    if (!GamesShared.getGames.exists(_.id.equals(gameSelected))) {
      Redirect(routes.GameController.index(), 302)
    } else {
      GamesShared.getGames.filter(_.id.equals(gameSelected)).headOption match {
        // No game found
        case None => BadRequest("")
        case Some(gameModel) => {
          gameModel.player += PlayerModel(user.userID, user.email.getOrElse(""))

          println(GamesShared.getGames.toString)
          println(user.email.getOrElse("") + " ist dem Spiel "
            + gameSelected + " beigetreten")
          Redirect(routes.GameController.game(), 302)
        }
      }
    }
  }

  def game = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.game.game(request.identity)))
  }

  def description = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.game.description()))
  }

  def rules = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.game.rules()))
  }

  def socket = WebSocket.acceptOrResult[String, String] { request =>
    implicit val req = Request(request, AnyContentAsEmpty)
    silhouette.SecuredRequestHandler { securedRequest =>
      Future.successful(HandlerResult(Ok, Some(securedRequest.identity)))
    }.map {
      case HandlerResult(r, Some(user)) => Right(ActorFlow.actorRef { out =>
        GamesShared.getGameWithPlayer(user.userID) match {
          case None => Props.empty
          case Some(game) => {
            println("Received a socket connection from Player: " + user.email.getOrElse(""))
            game.gameManager match {
              case None => Props.empty
              case Some(gameManager) => {
                println("Socket connection created: " + user.email.getOrElse(""))
                Props(new GameSocketActor(out, gameManager, user.userID))
              }
            }
          }
        }
      })
      case HandlerResult(r, None) => Left(Forbidden)
    }
  }
}
