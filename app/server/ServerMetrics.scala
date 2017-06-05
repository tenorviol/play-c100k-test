package server

import javax.inject._

import akka.actor.ActorSystem
import com.lightbend.cinnamon.akka.CinnamonMetrics
import com.lightbend.cinnamon.metric.Counter

@Singleton
class ServerMetrics @Inject() (system: ActorSystem) {

  val cinnamon = CinnamonMetrics(system)

  val connections: Counter = cinnamon.createCounter("ws.server.connection.count")

}
