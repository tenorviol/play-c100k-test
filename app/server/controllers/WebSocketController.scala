package server.controllers

import javax.inject._

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import play.api.Logger
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Controller, WebSocket}
import server.ServerMetrics

import scala.concurrent.ExecutionContext

object WebSocketController {

  object WebSocketEchoActor {
    def props(out: ActorRef, metrics: ServerMetrics): Props = {
      Props(classOf[WebSocketEchoActor], out, metrics)
    }
  }

  class WebSocketEchoActor(out: ActorRef, metrics: ServerMetrics) extends Actor {
    metrics.connections.increment()

    def receive: Receive = {
      case m: String =>
        // echo
        out ! m
    }

    override def postStop(): Unit = {
      metrics.connections.decrement()
    }
  }

}

@Singleton
class WebSocketController @Inject() (
  metrics: ServerMetrics
)(implicit system: ActorSystem, materializer: Materializer, ec: ExecutionContext) extends Controller {
  import WebSocketController._

  private val log = Logger(getClass)

  def wsEcho: WebSocket = WebSocket.accept[String, String] { request =>
    ActorFlow.actorRef(out => WebSocketEchoActor.props(out, metrics))
  }
}
