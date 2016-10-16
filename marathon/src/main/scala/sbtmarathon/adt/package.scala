package sbtmarathon

import scala.concurrent.duration.Duration
import org.json4sbt._
import org.json4sbt.JsonDSL._
import org.json4sbt.jackson.JsonMethods._

trait JsonSerializable {
  def json(): JValue
  def optJValue[A](opt: Option[A])(f: A => JValue): JValue = {
    opt.fold(JNothing: JValue)(f)
  }
}

package object adt {

type Request = String

object Request {

  def newBuilder(): Builder = Builder()

  case class Builder(
    id: Option[String] = None,
    cmdOrArgs: Option[CmdOrArgs] = None,
    cpus: Option[Double] = None,
    mem: Option[Double] = None,
    portDefinitions: Option[Seq[PortDefinition]] = None,
    requirePorts: Option[Boolean] = None,
    instances: Option[Int] = None,
    executor: Option[String] = None,
    container: Option[Container] = None,
    env: Option[Map[String, String]] = None,
    constraints: Option[Seq[Constraint]] = None,
    acceptedResourceRoles: Option[Seq[String]] = None,
    labels: Option[Map[String, String]] = None,
    fetch: Option[Seq[Fetchable]] = None,
    dependencies: Option[Seq[String]] = None,
    healthChecks: Option[Seq[HealthCheck]] = None,
    backoffSeconds: Option[Int] = None,
    backoffFactor: Option[Double] = None,
    maxLaunchDelaySeconds: Option[Int] = None,
    taskKillGracePeriodSeconds: Option[Int] = None,
    upgradeStrategy: Option[UpgradeStrategy] = None,
    ipAddress: Option[IpAddress] = None
  ) extends JsonSerializable {

    def withId(id: String): Builder = copy(id = Some(id))

    def withCmd(cmd: String): Builder = copy(cmdOrArgs = Some(Cmd(cmd)))

    def withArgs(args: Seq[String]): Builder = copy(cmdOrArgs = Some(Args(args)))

    def withCpus(cpus: Double): Builder = copy(cpus = Some(cpus))

    def withMem(mem: Double): Builder = copy(mem = Some(mem))

    def withInstances(n: Int): Builder = copy(instances = Some(n))

    def withExecutor(executor: String): Builder = copy(executor = Some(executor))

    def withContainer(container: Container): Builder = copy(container = Some(container))

    def withBackoff(duration: Duration): Builder = copy(backoffSeconds = Some(duration))

    def withBackoffSeconds(seconds: Int): Builder = copy(backoffSeconds = Some(seconds))

    def withBackoffFactor(factor: Double): Builder = copy(backoffFactor = Some(factor))

    def withMaxLaunchDelay(duration: Duration): Builder = copy(maxLaunchDelaySeconds = Some(duration))

    def withMaxLaunchDelaySeconds(seconds: Int): Builder = copy(maxLaunchDelaySeconds = Some(seconds))

    def withTaskKillGracePeriod(duration: Duration): Builder = copy(taskKillGracePeriodSeconds = Some(duration))

    def withTaskKillGracePeriodSeconds(seconds: Int): Builder = copy(taskKillGracePeriodSeconds = Some(seconds))

    def withUpgradeStrategy(strategy: UpgradeStrategy): Builder = copy(upgradeStrategy = Some(strategy))

    def withIpAddress(ipAddress: IpAddress): Builder = copy(ipAddress = Some(ipAddress))

    def enableRequirePorts(): Builder = copy(requirePorts = Some(true))

    def disableRequirePorts(): Builder = copy(requirePorts = Some(false))

    @deprecated("Prefer declaring ports using `container.portMappings`", since = "0.1.0")
    def addPortDefinition(portDefinition: PortDefinition): Builder = {
      copy(portDefinitions = portDefinitions.map(_ :+ portDefinition).orElse(Some(Seq(portDefinition))))
    }

    def addEnv(key: String, value: String): Builder = {
      val entry = key -> value
      copy(env = env.map(_ + entry).orElse(Some(Map(entry))))
    }

    def addConstraint(constraint: Constraint): Builder = {
      copy(constraints = constraints.map(_ :+ constraint).orElse(Some(Seq(constraint))))
    }

    def addAcceptedResourceRole(value: String): Builder = {
      copy(acceptedResourceRoles = acceptedResourceRoles.map(_ :+ value).orElse(Some(Seq(value))))
    }

    def addLabel(key: String, value: String): Builder = {
      val entry = key -> value
      copy(labels = labels.map(_ + entry).orElse(Some(Map(entry))))
    }

    def addFetch(fetchable: Fetchable): Builder = {
      copy(fetch = fetch.map(_ :+ fetchable).orElse(Some(Seq(fetchable))))
    }

    def addDependency(value: String): Builder = {
      copy(dependencies = dependencies.map(_ :+ value).orElse(Some(Seq(value))))
    }

    def addHealthCheck(healthCheck: HealthCheck): Builder = {
      copy(healthChecks = healthChecks.map(_ :+ healthCheck).orElse(Some(Seq(healthCheck))))
    }

    def json(): JValue = {
      val jvalues: Seq[JValue] = Seq(
        ("id" -> id),
        optJValue(cmdOrArgs)(_.json()),
        ("cpus" -> cpus),
        ("mem" -> mem),
        ("portDefinitions" -> optJValue(portDefinitions)(_.map(_.json()))),
        ("requirePorts" -> requirePorts),
        ("instances" -> instances),
        ("executor" -> executor),
        optJValue(container)(_.json()),
        ("env" -> env),
        ("constraints" -> optJValue(constraints)(_.map(_.json()))),
        ("acceptedResourceRoles" -> acceptedResourceRoles),
        ("labels" -> labels),
        ("fetch" -> optJValue(fetch)(_.map(_.json()))),
        ("dependencies" -> dependencies),
        ("healthChecks" -> optJValue(healthChecks)(_.map(_.json()))),
        ("backoffSeconds" -> backoffSeconds),
        ("backoffFactor" -> backoffFactor),
        ("maxLaunchDelaySeconds" -> maxLaunchDelaySeconds),
        ("taskKillGracePeriodSeconds" -> taskKillGracePeriodSeconds),
        optJValue(upgradeStrategy)(_.json()),
        optJValue(ipAddress)(_.json())
      )
      jvalues.reduce(_ merge _)
    }

    def build(): Request = {
      compact(render(json()))
    }

    private implicit def durationToSecondsInt(duration: Duration): Int = {
      scala.math.min(duration.toSeconds, Int.MaxValue).toInt
    }
  }
}

trait CmdOrArgs extends JsonSerializable

case class Cmd(cmd: String) extends CmdOrArgs {
  def json(): JValue = {
    ("cmd" -> cmd)
  }
}

case class Args(args: Seq[String]) extends CmdOrArgs {
  def json(): JValue = {
    ("args" -> args)
  }
}

case class PortDefinition(
  port: Int,
  protocol: String,
  name: Option[String] = None,
  labels: Option[Map[String, String]] = None
) extends JsonSerializable {

  def withName(value: String): PortDefinition = copy(name = Some(value))

  def addLabel(key: String, value: String) = {
    val entry = key -> value
    copy(labels = labels.map(_ + entry).orElse(Some(Map(entry))))
  }

  def json(): JValue = {
    val jvalues: Seq[JValue] = Seq(
      ("port" -> port),
      ("protocol" -> protocol),
      ("name" -> name),
      ("labels" -> labels)
    )
    jvalues.reduce(_ merge _)
  }
}

trait Container extends JsonSerializable

object Container {

  def fromJsonString(value: String): Container = new Container {
    def json(): JValue = {
      parse(value)
    }
  }

  case class PortMapping(
    containerPort: Int,
    hostPort: Int,
    servicePort: Option[Int] = None,
    protocol: String = "tcp"
  ) extends JsonSerializable {
    def json(): JValue = {
      ("containerPort" -> containerPort) ~
      ("hostPort" -> hostPort) ~
      ("servicePort" -> servicePort) ~
      ("protocol" -> protocol)
    }
  }

  case class Volume(
    containerPath: String,
    hostPath: String,
    mode: String
  ) extends JsonSerializable {
    def json(): JValue = {
      ("containerPath" -> containerPath) ~
      ("hostPath" -> hostPath) ~
      ("mode" -> mode)
    }
  }
}

case class DockerContainer(
  image: String,
  network: String,
  portMappings: Option[Seq[Container.PortMapping]] = None,
  privileged: Option[Boolean] = None,
  parameters: Option[Map[String, String]] = None,
  volumes: Option[Seq[Container.Volume]] = None
) extends Container {

  def enablePrivileged(): DockerContainer = copy(privileged = Some(true))

  def disablePrivileged(): DockerContainer = copy(privileged = Some(false))

  def addPortMapping(
    containerPort: Int,
    hostPort: Int,
    servicePort: Option[Int] = None,
    protocol: String = "tcp"
  ): DockerContainer = {
    val portMapping = Container.PortMapping(containerPort, hostPort, servicePort, protocol)
    addPortMapping(portMapping)
  }

  def addPortMapping(portMapping: Container.PortMapping): DockerContainer = {
    copy(portMappings = portMappings.map(_ :+ portMapping).orElse(Some(Seq(portMapping))))
  }

  def addParameter(key: String, value: String): DockerContainer = {
    val entry = key -> value
    copy(parameters = parameters.map(_ + entry).orElse(Some(Map(entry))))
  }

  def addVolume(
    containerPath: String,
    hostPath: String,
    mode: String
  ): DockerContainer = {
    val volume = Container.Volume(containerPath, hostPath, mode)
    addVolume(volume)
  } 

  def addVolume(volume: Container.Volume): DockerContainer = {
    copy(volumes = volumes.map(_ :+ volume).orElse(Some(Seq(volume))))
  }

  def json(): JValue = {
    ("container" ->
      ("type" -> "DOCKER") ~
      ("docker" ->
        ("image" -> image) ~
        ("network" -> network) ~
        ("portMappings" -> optJValue(portMappings)(_.map(_.json()))) ~
        ("privileged" -> privileged) ~
        ("parameters" -> parameters.map(
          _.map { case (key, value) => Map("key" -> key, "value" -> value) })
        )
      ) ~
      ("volumes" -> optJValue(volumes)(_.map(_.json())))
    )
  }
}

case class Constraint(args: String*) extends JsonSerializable {
  def json(): JValue = {
    (args: JValue)
  }
}

case class Fetchable(
  uri: String,
  executable: Option[Boolean] = None,
  extract: Option[Boolean] = None,
  cache: Option[Boolean] = None
) extends JsonSerializable {

  def enableExecutable(): Fetchable = copy(executable = Some(true))

  def disableExecutable(): Fetchable = copy(executable = Some(false))

  def enableExtract(): Fetchable = copy(extract = Some(true))

  def disableExtract(): Fetchable = copy(extract = Some(false))

  def enableCache(): Fetchable = copy(cache = Some(true))

  def disableCache(): Fetchable = copy(cache = Some(false))

  def json(): JValue = {
    ("uri" -> uri) ~ ("executable" -> executable) ~ ("extract" -> extract) ~ ("cache" -> cache)
  }
}

trait HealthCheck extends JsonSerializable

object HealthCheck {

  def fromJsonString(value: String): HealthCheck = new HealthCheck {
    def json(): JValue = {
      parse(value)
    }
  }

  case class Http(
    path: String,
    portIndex: Int,
    gracePeriodSeconds: Option[Int] = None,
    intervalSeconds: Option[Int] = None,
    timeoutSeconds: Option[Int] = None,
    maxConsecutiveFailures: Option[Int] = None,
    useHttps: Boolean = false
  ) extends HealthCheck with Config {
    val protocol  = if (useHttps) "HTTPS" else "HTTP"
    def json(): JValue = {
      ("path" -> path) ~ ("portIndex" -> portIndex) merge jsonConfig()
    }
  }

  case class Tcp(
    portIndex: Int,
    gracePeriodSeconds: Option[Int] = None,
    intervalSeconds: Option[Int] = None,
    timeoutSeconds: Option[Int] = None,
    maxConsecutiveFailures: Option[Int] = None
  ) extends HealthCheck with Config {
    val protocol = "TCP"
    def json(): JValue = {
      (("portIndex" -> portIndex): JValue) merge jsonConfig()
    }
  }

  case class Command(
    command: String,
    gracePeriodSeconds: Option[Int] = None,
    intervalSeconds: Option[Int] = None,
    timeoutSeconds: Option[Int] = None,
    maxConsecutiveFailures: Option[Int] = None
  ) extends HealthCheck with Config {
    val protocol = "COMMAND"
    def json(): JValue = {
      (("command" -> ("value" -> command)): JValue) merge jsonConfig()
    }
  }

  trait Config {
    this: HealthCheck =>
    def protocol: String
    def gracePeriodSeconds: Option[Int]
    def intervalSeconds: Option[Int]
    def timeoutSeconds: Option[Int]
    def maxConsecutiveFailures: Option[Int]
    protected def jsonConfig(): JValue = {
      ("protocol" -> protocol) ~
      ("gracePeriodSeconds" -> gracePeriodSeconds) ~
      ("intervalSeconds" -> intervalSeconds) ~
      ("timeoutSeconds" -> timeoutSeconds) ~
      ("maxConsecutiveFailures" -> maxConsecutiveFailures)
    }
  }
}

case class UpgradeStrategy(
  minimumHealthCapacity: Double = 1,
  maximumOverCapacity: Double = 1
) extends JsonSerializable {
  def json(): JValue = {
    ("upgradeStrategy" ->
      ("minimumHealthCapacity" -> minimumHealthCapacity) ~
      ("maximumOverCapacity" -> maximumOverCapacity)
    )
  }
}

case class IpAddress(
  groups: Option[Seq[String]] = None,
  labels: Option[Map[String, String]] = None,
  networkName: Option[String] = None
) extends JsonSerializable {

  def withNetworkName(name: String): IpAddress = copy(networkName = Some(name))

  def addGroup(group: String): IpAddress = {
    copy(groups = groups.map(_ :+ group).orElse(Some(Seq(group))))
  }

  def addLabel(key: String, value: String): IpAddress = {
    val entry = key -> value
    copy(labels = labels.map(_ + entry).orElse(Some(Map(entry))))
  }

  def json(): JValue = {
    ("ipAddress" ->
      ("groups" -> groups) ~
      ("labels" -> labels) ~
      ("networkName" -> networkName)
    )
  }
}

} // adt
