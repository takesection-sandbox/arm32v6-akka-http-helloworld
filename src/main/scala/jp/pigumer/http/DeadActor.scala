package jp.pigumer.http

import akka.actor.Actor
import akka.event.Logging

class DeadActor extends Actor {
  private val logger = Logging(context.system, this)
  override def receive: Receive = {
    case s ⇒
      logger.info(s"$s")
      context.system.deadLetters ! s
  }
}
