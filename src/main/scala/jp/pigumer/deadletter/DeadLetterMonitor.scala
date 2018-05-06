package jp.pigumer.deadletter

import akka.actor.{Actor, DeadLetter}
import akka.event.Logging

class DeadLetterMonitor extends Actor {
  private val logger = Logging(context.system, this)
  override def receive: Receive = {
    case DeadLetter(msg, sender, recipient) ⇒
      logger.info(s"deadletter $recipient $msg")
  }
}
