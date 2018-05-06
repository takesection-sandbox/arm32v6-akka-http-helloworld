package jp.pigumer.http

import akka.actor.Actor
import akka.event.Logging
import scala.concurrent.Future

class DeadActor extends Actor {
  private val logger = Logging(context.system, this)
  override def receive: Receive = {
    case s ⇒
      logger.info(s"$s")
      implicit val ec = context.dispatcher
      Future(throw new NullPointerException())
  }
}
