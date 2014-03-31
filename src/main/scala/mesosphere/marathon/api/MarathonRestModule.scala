package mesosphere.marathon.api

import java.util.logging.Logger

import mesosphere.chaos.http.RestModule
import com.google.inject.Scopes
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.common.collect.ImmutableMap
import org.eclipse.jgit.http.server.GitServlet

/**
 * @author Tobi Knaup
 */

class MarathonRestModule extends RestModule {

  val log = Logger.getLogger(getClass.getName)

  override val jacksonModules = Seq(
    DefaultScalaModule,
    new v2.json.MarathonModule
  )

  protected override def configureServlets() {
    // Initialize GitServlet.
    val params = ImmutableMap.of(
      "base-path", "/tmp",
      "export-all", "true",
      "aliases", "true"
    )
    serve("/git/*").`with`(classOf[GitServlet], params)

    super.configureServlets()

    bind(classOf[GitServlet]).in(Scopes.SINGLETON)
    bind(classOf[v2.GitResource]).in(Scopes.SINGLETON)

    // V1 API
    bind(classOf[v1.AppsResource]).in(Scopes.SINGLETON)
    bind(classOf[v1.DebugResource]).in(Scopes.SINGLETON)
    bind(classOf[v1.EndpointsResource]).in(Scopes.SINGLETON)
    bind(classOf[v1.TasksResource]).in(Scopes.SINGLETON)
    bind(classOf[v1.MarathonExceptionMapper]).asEagerSingleton()

    // V2 API
    bind(classOf[v2.AppsResource]).in(Scopes.SINGLETON)
    bind(classOf[v2.TasksResource]).in(Scopes.SINGLETON)
    bind(classOf[v2.EventSubscriptionsResource]).in(Scopes.SINGLETON)

    // This filter will redirect to the master if running in HA mode.
    bind(classOf[LeaderProxyFilter]).asEagerSingleton()
    filter("/*").through(classOf[LeaderProxyFilter])
  }

}
