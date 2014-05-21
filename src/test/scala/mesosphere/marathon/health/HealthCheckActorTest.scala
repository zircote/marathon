package mesosphere.marathon.health

import mesosphere.marathon.MarathonSpec
import akka.testkit.TestActorRef
import akka.actor.{ActorSystem, Props}
import mesosphere.marathon.tasks.TaskTracker


class HealthCheckActorTest extends MarathonSpec {

  import mesosphere.marathon.health.HealthCheckWorker._

  test("") {
    val healthCheck = HealthCheck()
    val taskTracker = mock[TaskTracker]


    implicit val system = ActorSystem("testsystem")
    val actor = TestActorRef(Props(classOf[HealthCheckActor], "testApp",
      healthCheck, taskTracker, None))

    val healthy = Healthy("task1")

    actor ! healthy
  }
}
