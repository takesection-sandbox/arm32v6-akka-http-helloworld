package jp.pigumer.http

import akka.NotUsed
import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.pattern.ask
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ClosedShape, FlowShape, SourceShape}
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait HelloWorldService {

  implicit val materializer: ActorMaterializer
  implicit val executionContext: ExecutionContext
  private implicit val timeout: Timeout = 10 seconds
  val dead: ActorRef
  val echo: ActorRef

  val helloWorldGraph = (src: Source[String, NotUsed]) ⇒
    RunnableGraph.fromGraph(GraphDSL.create(Sink.head[ToResponseMarshallable]) { implicit b ⇒
      sink ⇒
        import GraphDSL.Implicits._
        val source: SourceShape[String] = b.add(src)
        val helloWorld: FlowShape[String, ToResponseMarshallable] = b.add(
          Flow[String].mapAsync(1) {
            case "NPE" ⇒
              Future(throw new NullPointerException("error"))
            case "notfound" ⇒
              Future(HttpResponse(StatusCodes.NotFound))
            case s @ "dead" ⇒
              (dead ? s).mapTo[ToResponseMarshallable]
            case s ⇒
              (echo ? s).mapTo[String].map { res ⇒
                HttpEntity(ContentTypes.`text/plain(UTF-8)`, res)
              }
          })
        source ~> helloWorld ~> sink
        ClosedShape
    })
}
