package mesosphere.marathon.integration

import mesosphere.marathon.integration.util.MarathonEventListener

/**
 * The Scalatest Maven plugin is broken (tags don't work). So we can't
 * exclude integration tests from regular test runs and need to run them some
 * other way. That is why this main exists. Run with:
 *
 *  mvn -e scala:run -DmainClass="mesosphere.marathon.integration.Main" -Dexec.classpathScope="test"
 *
 * Start Marathon on localhost:8080 (the default), and with extra options
 * for http callbacks:
 *
 *  ./bin/start --master zk://localhost:2181/mesos --event_subscriber http_callback --http_endpoints http://localhost:9090
 */
object Main extends App {

  val listener = new MarathonEventListener()

  Seq(
    new CleanMarathonTest,
    new HealthCheckTest
  ).foreach(_.run())

  Thread.sleep(10000)

  sys.exit()
}
