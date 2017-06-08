package server.controllers

import javax.inject._

import akka.actor.ActorSystem
import akka.stream.scaladsl.Flow
import play.api.mvc.{Controller, WebSocket}
import server.ServerMetrics

import scala.concurrent.ExecutionContext

@Singleton
class WebSocketController @Inject() (
  metrics: ServerMetrics
)(implicit system: ActorSystem, ec: ExecutionContext) extends Controller {

  def wsEcho: WebSocket = WebSocket.accept[String, String] { request =>
    Flow[String].watchTermination() { (mat, doneFuture) =>
      metrics.connections.increment()
      doneFuture.onComplete { _ =>
        metrics.connections.decrement()
      }
      mat
    }
  }

}
