package client

import java.util.concurrent.atomic.AtomicInteger
import javax.inject._

import akka.actor.ActorSystem

import scala.collection.parallel.{immutable, mutable}

@Singleton
class AllClients @Inject() (metrics: ClientMetrics)(implicit system: ActorSystem) {

  private val nSequence = new AtomicInteger(0)
  private def nextN = nSequence.incrementAndGet()

  private val _clientSet = mutable.ParMap[Int, WebSocketClient]()

  def clientMap: immutable.ParMap[Int, WebSocketClient] = _clientSet.toMap

  /**
    * Returns the client number,
    * corresponding to the client map.
    */
  def add(ws: String): Int = synchronized {
    val n = nextN
    val client = new WebSocketClient(ws, metrics)
    _clientSet += (n -> client)
    n
  }

}
