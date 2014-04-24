package mesosphere.marathon.dns

import com.google.common.util.concurrent.AbstractService
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

import com.github.mkroli.dns4s.akka.Dns
import com.github.mkroli.dns4s.dsl._

import akka.actor._
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import javax.inject.Inject
import mesosphere.marathon.tasks.TaskTracker
import java.net.InetAddress
import com.github.mkroli.dns4s.section.ResourceRecord

/**
 * @author Tobi Knaup
 */

class DnsServerService @Inject() (implicit val system: ActorSystem,
                                  taskTracker: TaskTracker)
  extends AbstractService {

  override def doStart(): Unit = {
    implicit val timeout = Timeout(5 seconds)
    val props = Props(new DnsHandlerActor("foo.com", taskTracker))
    IO(Dns) ? Dns.Bind(system.actorOf(props), 5354)
  }

  override def doStop(): Unit = {

  }
}

class DnsHandlerActor(zone: String,
                      taskTracker: TaskTracker)
  extends Actor with ActorLogging {

  private val suffix = "." + zone

  override def receive = {
    case Query() ~ Questions(QName(host) ~ TypeA() :: Nil) =>
      log.info(s"Query for $host IN A")
      hosts(host) match {
        case Some(addresses) =>
          log.info(s"Responding with $addresses")
          sender ! Response ~ Questions(host) ~ Answers(ARecord(addresses.head))
        case _ =>
          sender ! Response ~ Questions(host) ~ Answers()
      }

  }

  private def hosts(host: String): Option[Iterable[String]] = {
    val index = host.lastIndexOf(suffix)
    if (index < 1) {
      return None
    }

    val appNameLowerCase = host.substring(0, index).toLowerCase
    taskTracker.list.values
      .find(_.appName.toLowerCase == appNameLowerCase)
      .map { app =>
        app.tasks.flatMap { task =>
          InetAddress.getAllByName(task.getHost).map(_.getHostAddress)
        }
      }
  }
}
