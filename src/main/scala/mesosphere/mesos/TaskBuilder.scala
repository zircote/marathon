package mesosphere.mesos

import org.apache.log4j.Logger
import java.io.ByteArrayOutputStream
import scala.collection._
import scala.collection.JavaConverters._
import org.apache.mesos.Protos._
import org.apache.mesos.Protos.Environment._
import mesosphere.marathon.api.v1.AppDefinition
import mesosphere.marathon.tasks.TaskTracker
import mesosphere.marathon._
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.protobuf.ByteString
import scala.util.Random
import scala.Some
import mesosphere.mesos.protos.{ RangesResource, ScalarResource, Resource }

class TaskBuilder(app: AppDefinition,
                  newTaskId: String => TaskID,
                  taskTracker: TaskTracker,
                  mapper: ObjectMapper = new ObjectMapper()) {

  import mesosphere.mesos.protos.Implicits._

  val log = Logger.getLogger(getClass.getName)

  def buildIfMatches(offer: Offer): Option[(TaskInfo, Seq[Long])] = {
    var cpuRole = ""
    var memRole = ""
    var diskRole = ""

    offerMatches(offer) match {
      case Some((cpu, mem, disk)) =>
        cpuRole = cpu
        memRole = mem
        diskRole = disk
      case _ =>
        log.info(s"No matching offer for ${app.id} (need ${app.cpus} CPUs, ${app.mem} mem, ${app.disk} disk, ${app.ports.size} ports) : " + offer)
        return None
    }

    val executor: Executor = if (app.executor == "") {
      Main.conf.executor
    }
    else {
      Executor.dispatch(app.executor)
    }

    val appPorts = app.ports.map(p => p: Int)

    TaskBuilder.getPorts(offer, appPorts).map { portsResource =>
      val ports = portsResource.ranges.flatMap(_.asScala)

      val taskId = newTaskId(app.id)
      val builder = TaskInfo.newBuilder
        .setName(taskId.getValue)
        .setTaskId(taskId)
        .setSlaveId(offer.getSlaveId)
        .addResources(ScalarResource(Resource.CPUS, app.cpus, cpuRole))
        .addResources(ScalarResource(Resource.MEM, app.mem, memRole))

      if (portsResource.ranges.nonEmpty) {
        builder.addResources(portsResource)
      }

      executor match {
        case CommandExecutor() => {
          builder.setCommand(TaskBuilder.commandInfo(app, ports))
        }

        case PathExecutor(path) => {
          val executorId = f"marathon-${taskId.getValue}" // Fresh executor
          val escaped = "'" + path + "'" // TODO: Really escape this.
          val shell = f"chmod ug+rx $escaped && exec $escaped ${app.cmd}"
          val command =
            TaskBuilder.commandInfo(app, ports).toBuilder.setValue(shell)

          val info = ExecutorInfo.newBuilder()
            .setExecutorId(ExecutorID.newBuilder().setValue(executorId))
            .setCommand(command)

          builder.setExecutor(info)
          val binary = new ByteArrayOutputStream()
          mapper.writeValue(binary, app)
          builder.setData(ByteString.copyFrom(binary.toByteArray))
        }
      }

      builder.build -> ports
    }
  }

  private def offerMatches(offer: Offer): Option[(String, String, String)] = {
    var cpuRole = ""
    var memRole = ""
    var diskRole = ""

    for (resource <- offer.getResourcesList.asScala) {
      if (cpuRole.isEmpty &&
        resource.getName == Resource.CPUS &&
        resource.getScalar.getValue >= app.cpus) {
        cpuRole = resource.getRole
      }
      if (memRole.isEmpty &&
        resource.getName == Resource.MEM &&
        resource.getScalar.getValue >= app.mem) {
        memRole = resource.getRole
      }
      if (diskRole.isEmpty &&
        resource.getName == Resource.DISK &&
        resource.getScalar.getValue >= app.disk) {
        diskRole = resource.getRole
      }
    }

    if (cpuRole.isEmpty || memRole.isEmpty || diskRole.isEmpty) {
      return None
    }

    if (app.constraints.nonEmpty) {
      val runningTasks = taskTracker.get(app.id)
      val constraintsMet = app.constraints.forall(
        Constraints.meetsConstraint(runningTasks, offer, _)
      )
      if (!constraintsMet) {
        log.warn("Did not meet a constraint in an offer.")
        return None
      }
      log.info("Met all constraints.")
    }
    Some((cpuRole, memRole, diskRole))
  }
}

object TaskBuilder {

  def commandInfo(app: AppDefinition, ports: Seq[Long]) = {
    val envMap = app.env ++ portsEnv(ports)

    val builder = CommandInfo.newBuilder()
      .setValue(app.cmd)
      .setEnvironment(environment(envMap))

    for (c <- app.container) {
      val container = CommandInfo.ContainerInfo.newBuilder()
        .setImage(c.image)
        .addAllOptions(c.options.asJava)
      builder.setContainer(container)
    }

    if (app.uris != null) {
      val uriProtos = app.uris.map(uri => {
        CommandInfo.URI.newBuilder()
          .setValue(uri)
          .build()
      })
      builder.addAllUris(uriProtos.asJava)
    }

    builder.build
  }

  def environment(vars: Map[String, String]) = {
    val builder = Environment.newBuilder()

    for ((key, value) <- vars) {
      val variable = Variable.newBuilder().setName(key).setValue(value)
      builder.addVariables(variable)
    }

    builder.build()
  }

  def getPorts(offer: Offer, appPorts: Seq[Int]): Option[RangesResource] = {
    offer.getResourcesList.asScala
      .find(_.getName == Resource.PORTS)
      .flatMap(getPorts(_, appPorts))
  }

  def getPorts(resource: org.apache.mesos.Protos.Resource,
               appPorts: Seq[Int]): Option[RangesResource] = {
    if (appPorts.isEmpty) {
      return Some(RangesResource(Resource.PORTS, Nil))
    }

    getAppPorts(resource, appPorts).orElse {
      getRandomPorts(resource, appPorts)
    }
  }

  def portsEnv(ports: Seq[Long]): Map[String, String] = {
    if (ports.isEmpty) {
      return Map.empty
    }

    val env = mutable.HashMap.empty[String, String]

    ports.zipWithIndex.foreach(p => {
      env += (s"PORT${p._2}" -> p._1.toString)
    })

    env += ("PORT" -> ports.head.toString)
    env += ("PORTS" -> ports.mkString(","))
    env
  }

  private def getAppPorts(resource: org.apache.mesos.Protos.Resource,
                          appPorts: Seq[Int]): Option[RangesResource] = {
    // Does the range contain all app ports?
    resource.getRanges.getRangeList.asScala.find { range =>
      appPorts.forall(port => range.getBegin <= port && range.getEnd >= port)
    }.map { range =>
      // Monotonically increasing ports
      val sortedPorts = appPorts.sorted
      if (sortedPorts.head + sortedPorts.size - 1 == sortedPorts.last) {
        RangesResource(Resource.PORTS, Seq(protos.Range(appPorts.head, appPorts.last)))
      }
      else {
        val portRanges = appPorts.map(p => protos.Range(p, p))
        RangesResource(Resource.PORTS, portRanges)
      }
    }
  }

  private def getRandomPorts(resource: org.apache.mesos.Protos.Resource,
                             appPorts: Seq[Int]): Option[RangesResource] = {
    val ranges = Random.shuffle(resource.getRanges.getRangeList.asScala)
    for (range <- ranges) {
      // TODO use multiple ranges if one is not enough
      if (range.getEnd - range.getBegin + 1 >= appPorts.length) {
        val maxOffset = (range.getEnd - range.getBegin - appPorts.length + 2).toInt
        val firstPort = range.getBegin.toInt + Random.nextInt(maxOffset)
        val rangeProto = protos.Range(firstPort, firstPort + appPorts.length - 1)
        return Some(
          RangesResource(Resource.PORTS, Seq(rangeProto), resource.getRole)
        )
      }
    }
    None
  }
}
