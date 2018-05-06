package jp.pigumer.http

import akka.actor.Actor
import akka.event.Logging
import scala.concurrent.Future

class EchoActor extends Actor {
  private val logger = Logging(context.system, this)
  override def receive: Receive = {
    case s ⇒
      val originalSender = sender
      implicit val ec = context.dispatcher
      logger.info(s"$originalSender $s")
      Future {
        s
      }.map(originalSender ! _)
  }
}
