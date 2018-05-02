package jp.pigumer.http

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.{ExecutionContext, Future}

class Server {

  def createSystem: ActorSystem = ActorSystem("helloworld")

  implicit val system = createSystem

  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val route =
    path(Segment) { message ⇒
      get {
        complete {
          val source: Source[String, NotUsed] = Source.single(message)
          val helloWorld: Flow[String, ToResponseMarshallable, NotUsed] = Flow[String].map {
            case "error" ⇒
              HttpResponse(StatusCodes.InternalServerError)
            case "notfound" ⇒
              HttpResponse(StatusCodes.NotFound)
            case "NPE" ⇒
              throw new NullPointerException()
            case s ⇒
              HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>${s}</h1>")
          }
          val sink: Sink[ToResponseMarshallable, Future[ToResponseMarshallable]] = Sink.head[ToResponseMarshallable]
          for (f <- source.via(helloWorld).runWith(sink)) yield f
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