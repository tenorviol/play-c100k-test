package client.controllers

import javax.inject._

import client.{AllClients, WebSocketClient}
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, ControllerComponents, Result}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ClientsController @Inject() (
  components: ControllerComponents,
  clients: AllClients
)(implicit ec: ExecutionContext) extends AbstractController(components) with I18nSupport {

  val addForm = Form(
    tuple(
      "ws" -> text,
      "count" -> default(number, 1)
    )
  )

  /**
    * Open one or more new `WebSocketClient`s connected to a single WebSocket server.
    */
  def post = Action { implicit request =>
    val form = addForm.bindFromRequest()
    form.fold(f => BadRequest(f.errorsAsJson), { case (ws, count) =>
      val ens = for (i <- 0 until count) yield {
        clients.add(ws)
      }
      val body = ens.map(client.controllers.routes.ClientsController.getN).map(_.toString)
      Ok(Json.toJson(body))
    })
  }

  var NotFoundResult = NotFound("404 Not Found")

  case class WithClient(n: Int) {
    def apply(f: WebSocketClient => Result): Result = {
      clients.clientMap.get(n).fold(NotFoundResult)(f)
    }
    def async(f: WebSocketClient => Future[Result]): Future[Result] = {
      clients.clientMap.get(n).fold(Future.successful(NotFoundResult))(f)
    }
  }

  private def clientResult(n: Int, wsClient: WebSocketClient): Result = {
    val call = client.controllers.routes.ClientsController.getN(n)
    val data = Json.obj(
      "url" -> call.url,
      "ws" -> wsClient.ws,
      "connected" -> wsClient.isConnected,
      "sent" -> wsClient.sent,
      "received" -> wsClient.received
    )
    Ok(Json.prettyPrint(data))
  }

  /**
    * Get data on a specific `WebSocketClient`.
    */
  def getN(n: Int) = Action {
    WithClient(n) { client =>
      clientResult(n, client)
    }
  }

  /**
    * Send a text message to the WebSocket server.
    */
  def postN(n: Int) = Action(parse.text).async { request =>
    WithClient(n).async { client =>
      client.send(request.body).map { _ =>
        clientResult(n, client)
      }
    }
  }

  /**
    * Disconnect from the WebSocket server.
    */
  def deleteN(n: Int) = Action.async {
    WithClient(n).async { client =>
      client.close().map { _ =>
        clientResult(n, client)
      }
    }
  }

}
