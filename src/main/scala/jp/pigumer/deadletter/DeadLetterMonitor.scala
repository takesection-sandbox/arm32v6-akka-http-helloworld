package jp.pigumer.deadletter

import akka.actor.{ActorRef, DeadLetter}
import akka.event.LoggingAdapter
import akka.stream._
import akka.stream.scaladsl.{Sink, Source, SourceQueueWithComplete}

trait DeadLetterMonitor {
  protected val logger: LoggingAdapter
  implicit val materializer: ActorMaterializer

  lazy val deadLetterMonitor: ActorRef =
    Source.actorRef[DeadLetter](100, OverflowStrategy.dropTail)
      .to(Sink.foreach {
        case DeadLetter(message, sender, recipient) ⇒
          logger.info(s"$message - $recipient")
      }).run()

  lazy val deadLetterQueue: SourceQueueWithComplete[DeadLetter] =
    Source.queue[DeadLetter](100, OverflowStrategy.backpressure)
      .to(Sink.foreach {
        case DeadLetter(message, sender, recipient) ⇒
          logger.info(s"$message - $recipient")
      }).run()
}
