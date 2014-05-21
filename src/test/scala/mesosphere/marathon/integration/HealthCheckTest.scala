package mesosphere.marathon.integration

import mesosphere.marathon.api.v2.json.EnrichedTask


class HealthCheckTest extends IntegrationTest {

  def run() {
    healthyTest()
    unhealthyTest()
  }

  def healthyTest() {
    val createApp = post("/v2/apps",
      """{
        "id": "healthy",
        "cmd": "python -m SimpleHTTPServer $PORT",
        "mem": 50,
        "cpus": 0.1,
        "instances": 1,
        "healthChecks": [
          {
            "protocol": "HTTP",
            "portIndex": 0
          }
        ]
      }""")

    assert(createApp.status.isSuccess, s"should be success but was $createApp")

    Thread.sleep(1000)

    val tasks = getTasks("healthy")

    assert(tasks.forall(t => t.healthCheckResults.forall(h => h.get.alive())),
      "All tasks should be healthy")

    delete("/v2/apps/healthy")
  }

  def unhealthyTest() {
    // Create an app that doesn't open a socket so the health check fails
    val createApp = post("/v2/apps",
      """{
        "id": "unhealthy",
        "cmd": "sleep 600",
        "mem": 50,
        "cpus": 0.1,
        "instances": 1,
        "healthChecks": [
          {
            "protocol": "HTTP",
            "portIndex": 0,
            "gracePeriodSeconds": 2,
            "intervalSeconds": 1,
            "maxConsecutiveFailures": 3
          }
        ]
      }""")

    assert(createApp.status.isSuccess, s"should be success but was $createApp")

//    val event = Main.listener.waitForEvent("api_post_event")
//    println(s"CLEAN $event")

    Thread.sleep(1000)
    val tasks = getTasks("unhealthy")
    assert(tasks.forall(t => t.healthCheckResults.forall(h => h.get.consecutiveFailures == 0)),
      "Shouldn't count failures during grace period")

    Thread.sleep(1500)
    val tasks2 = getTasks("unhealthy")
    assert(tasks2.forall(t => t.healthCheckResults.forall(h => h.get.consecutiveFailures > 0)),
      "Should count failures after grace period")

    delete("/v2/apps/unhealthy")
  }

  private def getTasks(appId: String) = {
    val getAppTasks = get(s"/v2/apps/$appId/tasks")
    val json = parseJSON(getAppTasks.entity.asString)
    (json \ "app" \ "tasks").extract[List[EnrichedTask]]
  }
}
