package controllers

import javax.inject._

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import play.api.Logger
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Controller, WebSocket}

import scala.concurrent.ExecutionContext

object WebSocketController {

  object WebSocketEchoActor {
    def props(out: ActorRef) = Props(classOf[WebSocketEchoActor], out)
  }

  class WebSocketEchoActor(out: ActorRef) extends Actor {
    def receive: Receive = {
      case m: String =>
        // echo
        out ! m
    }

    override def postStop(): Unit = {
      println("Finished!!!")
    }
  }

}

@Singleton
class WebSocketController @Inject() (implicit system: ActorSystem, materializer: Materializer, ec: ExecutionContext) extends Controller {
  import WebSocketController._

  private val log = Logger(getClass)

  def wsEcho: WebSocket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => WebSocketEchoActor.props(out))
  }
}
