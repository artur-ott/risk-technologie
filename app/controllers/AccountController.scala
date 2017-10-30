package controllers

import javax.inject._

import play.api.mvc._

/**
 * This controller demonstrates how to use dependency injection to
 * bind a component into a controller class. The class creates an
 * `Action` that shows an incrementing count to users. The [[Counter]]
 * object is injected by the Guice dependency injection system.
 */
@Singleton
class AccountController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {
    def index = Action {
      Ok(views.html.account.index())
    }
}