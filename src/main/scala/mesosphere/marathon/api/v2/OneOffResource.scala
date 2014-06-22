package mesosphere.marathon.api.v2

import javax.ws.rs.{ Produces, POST, Consumes, Path }
import javax.ws.rs.core.{ Response, MediaType }
import javax.inject.Inject
import com.codahale.metrics.annotation.Timed
import javax.validation.Valid
import mesosphere.marathon.tasks.TaskQueue
import mesosphere.marathon.api.v1.AppDefinition

@Path("v2/run")
@Consumes(Array(MediaType.APPLICATION_JSON))
class OneOffResource @Inject() (taskQueue: TaskQueue) {

  @POST
  @Timed
  @Produces(Array(MediaType.APPLICATION_JSON))
  def create(@Valid task: OneOffTask) = {
    val appDef = AppDefinition(id = "one-off", cmd = task.cmd)
    taskQueue.add(appDef)
    Response.noContent()
  }
}
