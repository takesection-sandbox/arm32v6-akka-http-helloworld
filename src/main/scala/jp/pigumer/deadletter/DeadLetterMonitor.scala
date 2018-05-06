package jp.pigumer.deadletter

import akka.actor.{Actor, ActorRef, DeadLetter}
import akka.event.Logging
import akka.stream._
import akka.stream.scaladsl.{Flow, Sink, Source}

class DeadLetterMonitor extends Actor {
  private val logger = Logging(context.system, this)
  private implicit val materializer = ActorMaterializer()(context)
  private val src: Source[DeadLetter, ActorRef] = Source.actorRef[DeadLetter](100, OverflowStrategy.fail)
  private val flow: ActorRef = Flow[DeadLetter].to(Sink.foreach {
    case DeadLetter(message, sender, recipient) ⇒
      logger.info(s"deadletter $recipient $message")
  }).runWith(src)
  override def receive: Receive = {
    case deadLetter ⇒
      flow ! deadLetter
  }
}
