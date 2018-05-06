package jp.pigumer.http

import akka.actor.Actor

class DeadActor extends Actor {
  override def receive: Receive = Actor.emptyBehavior
}
