package jp.pigumer.deadletter

import akka.Done
import akka.actor.{Actor, DeadLetter}
import akka.event.Logging
import akka.stream._
import akka.stream.scaladsl.{Keep, Sink, Source, SourceQueueWithComplete}

import scala.concurrent.Future

class DeadLetterMonitor extends Actor {
  private val logger = Logging(context.system, this)
  private implicit val materializer = ActorMaterializer()(context)

  private val src: Source[DeadLetter, SourceQueueWithComplete[DeadLetter]] =
    Source.queue[DeadLetter](100, OverflowStrategy.backpressure)
  private val sink: Sink[Any, Future[Done]] = Sink.ignore
  private val (queue, _) = src
    .map {
      case DeadLetter(message, sender, recipient) ⇒
        logger.info(s"$message - $recipient")
    }.toMat(sink)(Keep.both).run()

  override def receive: Receive = {
    case deadLetter: DeadLetter ⇒
      queue.offer(deadLetter)
  }
}
