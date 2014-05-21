package mesosphere.marathon.integration.util

import akka.actor.{ActorLogging, Actor}
import spray.http.{HttpResponse, HttpRequest}
import spray.http.HttpMethods._
import java.util
import spray.can.Http


class EventListenerActor(val queue: util.Queue[String])
  extends Actor with ActorLogging {

  def receive = {
    // when a new connection comes in we register ourselves as the connection handler
    case _: Http.Connected =>
      sender ! Http.Register(self)

    case HttpRequest(POST, uri, header, entity, protocol) =>
      log.info(s"Received event: $entity")
      queue.add(entity.asString)
      sender ! HttpResponse(entity = "SUP")
  }
}
