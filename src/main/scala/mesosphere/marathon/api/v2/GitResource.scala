package mesosphere.marathon.api.v2

import scala.Array
import scala.language.postfixOps
import javax.ws.rs._
import javax.ws.rs.core.{Context, MediaType}
import javax.servlet.http.HttpServletRequest
import java.util.logging.Logger
import org.eclipse.jgit.internal.storage.file.FileRepository

@Path("v2/git")
@Produces(Array(MediaType.APPLICATION_JSON))
@Consumes(Array(MediaType.APPLICATION_JSON))
class GitResource {
  val log = Logger.getLogger(getClass.getName)

  @POST
  @Path("/create")
  def create(@Context req: HttpServletRequest, @QueryParam("name") repoName: String) = {
    val basePath = "/tmp/"
    val newDB = new FileRepository(basePath + repoName)
    newDB.create(true)
  }

}
