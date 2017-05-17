package controllers

import javax.inject._

import akka.actor.ActorSystem
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._

import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._

@Singleton
class HomeController @Inject() (system: ActorSystem) extends Controller {

  def index = Action.async {
    val start = System.currentTimeMillis
    val result = Promise[Result]
    system.scheduler.scheduleOnce(10.seconds) {
      val elapsed = (System.currentTimeMillis - start).millis
      result.success(
        Ok(s"Completed in $elapsed")
      )
    }
    result.future
  }

}
