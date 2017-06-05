package client

import java.time.{Duration, Instant}
import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.ActorSystem
import akka.Done
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, OverflowStrategy, QueueOfferResult}
import akka.stream.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import play.api.Logger

import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._
import scala.util.Random

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

  private val _isClosed = new AtomicBoolean(false)
  def isClosed: Boolean = _isClosed.get

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

  private val PingPeriod = 30.seconds
  private val PingRE = """Ping (\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d.\d\d\dZ)""".r

  // print each incoming message
  private val receiveSink: Sink[Message, Future[Done]] = Sink.foreach {
    case m: TextMessage.Strict =>
      _received.prepend(m.text)
      m.text match {
        case PingRE(i) =>
          val now = Instant.now
          val start = Instant.parse(i)
          val latency = Duration.between(start, now)
          metrics.ping.record(latency.toMillis)
        case _ =>
      }
    case m: Message =>
      _received.prepend(m.toString)
  }

  // the Future[Done] is the materialized value of Sink.foreach
  // and it is completed when the stream completes
  private val flow: Flow[Message, Message, Future[Done]] = {
    Flow
      .fromSinkAndSourceMat(receiveSink, sendSource)(Keep.left)
      .watchTermination() { (mat, doneFuture) =>
        metrics.connections.increment()
        val ping = system.scheduler.schedule(Random.nextInt(PingPeriod.toMillis.toInt).millis, PingPeriod) {
          val now = Instant.now
          send(s"Ping $now")
        }

        doneFuture.onComplete { _ =>
          _isClosed.set(true)
          metrics.connections.decrement()
          ping.cancel()
        }

        mat
      }
  }

  private val connectionStart = Instant.now

  // `upgradeResponse` is a Future[WebSocketUpgradeResponse] that
  // completes or fails when the connection succeeds or fails.
  private val (upgradeResponse, _) = {
    Http().singleWebSocketRequest(WebSocketRequest(ws), flow)
  }

  val connectionResponse: Future[HttpResponse] = upgradeResponse.map { upgrade =>
    val connectionEstablished = Instant.now()
    metrics.connectionMS.record(Duration.between(connectionStart, connectionEstablished).toMillis)
    upgrade.response
  }

}
