package client.controllers

import javax.inject._

import client.AllClients
import play.api.mvc.{Action, Controller}

@Singleton
class HomeController @Inject() (clients: AllClients) extends Controller {

  def get = Action {
    Ok(
      s"""Client count = ${clients.clientMap.size}
       """.stripMargin)
  }

}
