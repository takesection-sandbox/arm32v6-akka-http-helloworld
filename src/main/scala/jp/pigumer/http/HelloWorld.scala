package jp.pigumer.http

import akka.actor.{ActorRef, ActorSystem, DeadLetter, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import jp.pigumer.deadletter.DeadLetterMonitor

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class Server extends HelloWorldService {

  def createSystem: ActorSystem = ActorSystem("helloworld")
  implicit val system: ActorSystem = createSystem

  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  private val logger = Logging(system, "HelloWorld")

  private val monitor = system.actorOf(Props[DeadLetterMonitor])
  override val echo: ActorRef = system.actorOf(Props[EchoActor], "echo")
  override val dead: ActorRef = system.actorOf(Props[DeadActor], "dead")
  system.stop(dead)

  system.eventStream.subscribe(monitor, classOf[DeadLetter])

  private implicit val timeout: Timeout = 30 seconds

  val route: Route =
    path(Segment) { message ⇒
      get {
        onComplete {
          helloWorldGraph(Source.single(message)).run()
        } {
          case Success(value) ⇒
            complete(value)
          case Failure(cause) ⇒
            logger.error(cause, "HelloWorld")
            throw cause
        }
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
    implicit val executionContext: ExecutionContext = server.executionContext
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}