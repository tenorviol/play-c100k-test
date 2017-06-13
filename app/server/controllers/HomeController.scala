package server.controllers

import javax.inject._

import play.api.mvc.{Action, Controller}

@Singleton
class HomeController extends Controller {

  def index = Action {
    Ok("play-c100k-server")
  }

}
