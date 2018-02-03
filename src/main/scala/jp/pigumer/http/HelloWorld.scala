package jp.pigumer.http

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._

import scala.concurrent.Future

class Server {

  def createSystem: ActorSystem = ActorSystem("helloworld")

  implicit val system = createSystem

  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val route =
    pathEndOrSingleSlash {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }

  def run: Future[Http.ServerBinding] =
   Http().bindAndHandle(route, "0.0.0.0", 8080)
}

object HelloWorld extends App {
  val server = new Server
  val bindingFuture = server.run

  sys.addShutdownHook {
    val system = server.system
    implicit val executionContext = server.executionContext
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}