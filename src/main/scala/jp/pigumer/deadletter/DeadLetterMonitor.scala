package jp.pigumer.deadletter

import akka.actor.Actor
import akka.event.Logging

class DeadLetterMonitor extends Actor {
  private val logger = Logging(context.system, this)
  override def receive: Receive = {
    case msg ⇒
      logger.info(s"deadletter $msg")
  }
}
