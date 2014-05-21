package mesosphere.marathon.integration.util

import akka.actor.{Props, ActorSystem}
import spray.can.Http
import akka.io.IO
import java.util.concurrent.{LinkedBlockingQueue, TimeUnit}
import org.apache.log4j.Logger


class MarathonEventListener(port: Int = 9090) {

  val log = Logger.getLogger(getClass.getName)

  val queue = new LinkedBlockingQueue[String]()
  val pollTimeoutSeconds = 5

  implicit val system = ActorSystem("IntegrationTestServer")
  val listener = system.actorOf(Props(classOf[EventListenerActor], queue))

  IO(Http) ! Http.Bind(listener, interface = "localhost", port = port)

  def waitForEvent(eventType: String, tries: Int = 10): String = {
    for (i <- 0 to 10) {
      val event = queue.poll(pollTimeoutSeconds, TimeUnit.SECONDS)

      if (event == null) {
        log.warn(s"Queue was empty waiting for $eventType")
      } else if (event.matches(eventType)) {
        return event
      }
    }
    throw new RuntimeException(s"No $eventType in the queue after $tries tries")
  }
}
