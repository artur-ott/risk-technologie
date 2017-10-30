package controllers

import javax.inject._

import play.api.mvc._

import de.htwg.se.scala_risk.model.impl._

/**
 * This controller demonstrates how to use dependency injection to
 * bind a component into a controller class. The class creates an
 * `Action` that shows an incrementing count to users. The [[Counter]]
 * object is injected by the Guice dependency injection system.
 */
@Singleton
class GeneralController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an action that responds with the [[Counter]]'s current
   * count. The result is plain text. This `Action` is mapped to
   * `GET /count` requests by an entry in the `routes` config file.
   */
  def story = Action {
      Ok(views.html.general.story())
      }

  def index = Action {
      Ok("<h1 style=\"color: #f00;\">" + Colors.RED.toString + "</h1>").as(HTML)
      }
  
}
