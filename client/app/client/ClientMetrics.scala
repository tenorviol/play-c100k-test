package client

import javax.inject._

import akka.actor.ActorSystem
import com.lightbend.cinnamon.akka.{CinnamonMetrics, Timer}
import com.lightbend.cinnamon.metric._

import scala.concurrent.ExecutionContext

@Singleton
class ClientMetrics @Inject() (system: ActorSystem)(implicit ec: ExecutionContext) {

  val cinnamon = CinnamonMetrics(system)

  val connections: Counter = cinnamon.createCounter("ws.clients.connections.count")

  val connectionMS: Recorder = cinnamon.createRecorder("ws.clients.connection.ms")

}
