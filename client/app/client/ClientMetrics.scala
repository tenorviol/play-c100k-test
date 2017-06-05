package client

import javax.inject._

import akka.actor.ActorSystem
import com.lightbend.cinnamon.akka.CinnamonMetrics
import com.lightbend.cinnamon.metric._

@Singleton
class ClientMetrics @Inject() (system: ActorSystem) {

  val cinnamon = CinnamonMetrics(system)

  val connections: Counter = cinnamon.createCounter("ws.client.connection.count")

  val connectionMS: Recorder = cinnamon.createRecorder("ws.client.connection.ms")

}
