package jp.pigumer.http

import akka.NotUsed
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ClosedShape, FlowShape, SourceShape}

import scala.concurrent.ExecutionContext

trait HelloWorldService {

  implicit val materializer: ActorMaterializer
  implicit val executionContext: ExecutionContext

  val helloWorldGraph = (src: Source[String, NotUsed]) ⇒
    RunnableGraph.fromGraph(GraphDSL.create(Sink.head[ToResponseMarshallable]) { implicit b ⇒
      sink ⇒
        import GraphDSL.Implicits._
        val source: SourceShape[String] = b.add(src)
        val helloWorld: FlowShape[String, ToResponseMarshallable] = b.add(
          Flow[String].map {
            case "error" ⇒
              HttpResponse(StatusCodes.InternalServerError)
            case "notfound" ⇒
              HttpResponse(StatusCodes.NotFound)
            case "NPE" ⇒
              throw new NullPointerException()
            case s ⇒
              HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>$s</h1>")
          })
        source ~> helloWorld ~> sink
        ClosedShape
    })
}
