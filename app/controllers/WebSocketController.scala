package controllers

import akka.NotUsed
import akka.stream.scaladsl.Flow
import play.api.Logger
import play.api.mvc.{Controller, WebSocket}

import scala.concurrent.Future

class WebSocketController extends Controller {
  private val log = Logger(getClass)

  def wsEcho: WebSocket = WebSocket.acceptOrResult[String, String] { request =>
    val echoFlow: Flow[String, String, NotUsed] = Flow[String]
    Future.successful(
      Right(echoFlow)
    )
  }

}
