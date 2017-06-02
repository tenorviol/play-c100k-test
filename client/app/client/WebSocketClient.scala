package client

import java.time.{Duration, Instant}

import akka.actor.ActorSystem
import akka.Done
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, OverflowStrategy, QueueOfferResult}
import akka.stream.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import play.api.Logger

import scala.concurrent.{Future, Promise}

object WebSocketClient {

  private val log = Logger(classOf[WebSocketClient])

  type SendQueue = SourceQueueWithComplete[Message]

  class History[T](val size: Int) {
    @volatile private var _history = Seq.empty[T]

    def history: Seq[T] = _history

    def prepend(t: T): Unit = synchronized {
      _history = (Seq(t) ++ _history).take(size)
    }
  }

}

/**
  * E.g. "ws://echo.websocket.org"
  */
case class WebSocketClient(ws: String, metrics: ClientMetrics)(implicit system: ActorSystem) {
  import WebSocketClient._

  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  def isConnected: Boolean = connectionResponse.isCompleted && !isClosed

  def isClosed: Boolean = closed.isCompleted

  def send(s: String): Future[QueueOfferResult] = {
    sendQueue.flatMap { q =>
      _sent.prepend(s)
      q.offer(TextMessage(s))
    }
  }

  def close(): Future[Unit] = {
    sendQueue.map(_.complete())
  }

  private val _sent = new History[String](5)
  def sent: Seq[String] = _sent.history

  private val _received = new History[String](5)
  def received: Seq[String] = _received.history

  private val (sendSource, sendQueue): (Source[Message, SendQueue], Future[SendQueue]) = {
    val queue = Promise[SendQueue]
    val source = Source
      .queue[Message](1, OverflowStrategy.backpressure)
      .mapMaterializedValue { mat =>
        queue.trySuccess(mat)
        mat
      }
    (source, queue.future)
  }

  // print each incoming message
  private val receiveSink: Sink[Message, Future[Done]] = Sink.foreach {
    case m: TextMessage.Strict =>
      _received.prepend(m.text)
    case m: Message =>
      _received.prepend(m.toString)
  }

  // the Future[Done] is the materialized value of Sink.foreach
  // and it is completed when the stream completes
  private val flow: Flow[Message, Message, Future[Done]] = {
    Flow.fromSinkAndSourceMat(receiveSink, sendSource)(Keep.left)
  }

  private val connectionStart = Instant.now

  // upgradeResponse is a Future[WebSocketUpgradeResponse] that
  // completes or fails when the connection succeeds or fails
  // and closed is a Future[Done] representing the stream completion from above
  private val (upgradeResponse, closed) = {
    Http().singleWebSocketRequest(WebSocketRequest(ws), flow)
  }

  val connectionResponse: Future[HttpResponse] = upgradeResponse.map { upgrade =>
    val connectionEstablished = Instant.now()
    metrics.connectionMS.record(Duration.between(connectionStart, connectionEstablished).toMillis)
    metrics.connections.increment()
    upgrade.response
  }

  closed.foreach { _ =>
    metrics.connections.decrement()
  }

}
