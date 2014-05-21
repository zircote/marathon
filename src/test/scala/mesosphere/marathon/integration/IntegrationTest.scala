package mesosphere.marathon.integration

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.util.Timeout
import akka.pattern.ask
import akka.io.IO

import spray.can.Http
import spray.http._
import HttpMethods._
import org.json4s.{DefaultFormats, Formats}


trait IntegrationTest extends Runnable {

  import org.json4s._
  import org.json4s.jackson.JsonMethods._

  implicit val system: ActorSystem = ActorSystem("IntegrationTestClient")
  implicit val timeout: Timeout = Timeout(15 seconds)
  import system.dispatcher // implicit execution context

  val marathonPort = 8080

  implicit def json4sFormats: Formats = DefaultFormats


  def get(path: String): HttpResponse = {
    val request = HttpRequest(method = GET, uri = uri(path))
    perform(request)
  }

  def post(path: String, body: String): HttpResponse = {
    val request = HttpRequest(method = HttpMethods.POST, uri = uri(path))
      .withEntity(HttpEntity(ContentTypes.`application/json`, body))
    perform(request)
  }

  def delete(path: String): HttpResponse = {
    val request = HttpRequest(method = DELETE, uri = uri(path))
    perform(request)
  }

  def perform(request: HttpRequest): HttpResponse = {
    val response: Future[HttpResponse] =
      (IO(Http) ? request).mapTo[HttpResponse]
    Await.result(response, timeout.duration)
  }

  def uri(path: String): String = {
    s"http://localhost:$marathonPort$path"
  }

  def parseJSON(json: String) = {
    parse(json)
  }
}
