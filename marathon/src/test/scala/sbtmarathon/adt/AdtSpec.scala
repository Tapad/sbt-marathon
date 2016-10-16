package sbtmarathon.adt

import scala.concurrent.duration.Duration
import org.json4sbt._
import org.json4sbt.jackson.JsonMethods._
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.matchers.{Matcher, MatchResult}

class AdtSpec extends FlatSpec with Matchers {

  behavior of "Request.Builder"

  it should "create Marathon JSON requests using fluent interface" in {
    val builder = Request.newBuilder()
      .withId("/product/service/myApp")
      .withCmd("env && sleep 300")
      .withCpus(1.5)
      .withMem(256)
      .addPortDefinition(
        PortDefinition(port = 8080, protocol = "tcp").withName("http").addLabel("VIP_0", "10.0.0.1:80")
      )
      .addPortDefinition(
        PortDefinition(port = 9000, protocol = "tcp").withName("admin")
      )
      .disableRequirePorts()
      .withInstances(3)
      .withExecutor("")
      .withContainer(
        DockerContainer(image = "group/image", network = "BRIDGE")
          .disablePrivileged()
          .addPortMapping(containerPort = 8080, hostPort = 0, servicePort = Some(9000), protocol = "tcp")
          .addPortMapping(containerPort = 161, hostPort = 0, protocol = "udp")
          .addParameter("a-docker-option", "xxx")
          .addParameter("b-docker-option", "yyy")
          .addVolume(containerPath = "/etc/a", hostPath = "/var/data/a", mode = "RO")
          .addVolume(containerPath = "/etc/b", hostPath = "/var/data/b", mode = "RW")
      )
      .addEnv("LD_LIBRARY_PATH", "/usr/local/lib/myLib")
      .addConstraint(
        Constraint("attribute-1", "OPERATOR", "value-1")
      )
      .addConstraint(
        Constraint("attribute-2", "OPERATOR", "value-2")
      )
      .addAcceptedResourceRole("role1")
      .addAcceptedResourceRole("*")
      .addLabel("environment", "staging")
      .addFetch(
        Fetchable(uri = "https://raw.github.com/mesosphere/marathon/master/README.md")
      )
      .addFetch(
        Fetchable(uri = "https://foo.com/archive.zip")
          .disableExecutable()
          .enableExtract()
          .enableCache()
      )
      .addDependency("/product/db/mongo")
      .addDependency("/product/db")
      .addDependency("../../db")
      .addHealthCheck(
        HealthCheck.Http(
          path = "/health",
          portIndex = 0,
          gracePeriodSeconds = Some(3),
          intervalSeconds = Some(10),
          timeoutSeconds = Some(10),
          maxConsecutiveFailures = Some(3)
        )
      )
      .addHealthCheck(
        HealthCheck.fromJsonString(
          """
          { "protocol": "HTTPS"
          , "path": "/machinehealth"
          , "port": 3333
          , "gracePeriodSeconds": 3
          , "intervalSeconds": 10
          , "timeoutSeconds": 10
          , "maxConsecutiveFailures": 3
          }
          """
        )
      )
      .addHealthCheck(
        HealthCheck.Tcp(
          portIndex = 1,
          gracePeriodSeconds = Some(3),
          intervalSeconds = Some(5),
          timeoutSeconds = Some(5),
          maxConsecutiveFailures = Some(3)
        )
      )
      .addHealthCheck(
        HealthCheck.Command(
          command = "curl -f -X GET http://$HOST:$PORT0/health",
          maxConsecutiveFailures = Some(3)
        )
      )
      .withBackoffSeconds(1)
      .withBackoffFactor(1.15)
      .withMaxLaunchDelay(Duration("1 hour"))
      .withTaskKillGracePeriodSeconds(2)
      .withUpgradeStrategy(
        UpgradeStrategy(minimumHealthCapacity = 0.5, maximumOverCapacity = 0.2)
      )
      .withIpAddress(
        IpAddress()
          .withNetworkName("dev-network")
          .addGroup("backend")
          .addLabel("color", "purple")
          .addLabel("flavor", "grape")
          .addLabel("org", "product")
          .addLabel("service", "myApp")
          .addLabel("tier", "backend")
      )

    builder.json() should matchJsonString(
      """
      { "id": "/product/service/myApp"
      , "cmd": "env && sleep 300"
      , "cpus": 1.5
      , "mem": 256.0
      , "portDefinitions":
        [ { "port": 8080, "protocol": "tcp", "name": "http", "labels": { "VIP_0": "10.0.0.1:80" } }
        , { "port": 9000, "protocol": "tcp", "name": "admin" }
        ]
      , "requirePorts": false
      , "instances": 3
      , "executor": ""
      , "container":
        { "type": "DOCKER"
        , "docker":
          { "image": "group/image"
          , "network": "BRIDGE"
          , "portMappings":
            [ { "containerPort": 8080
              , "hostPort": 0
              , "servicePort": 9000
              , "protocol": "tcp"
              }
            , { "containerPort": 161
              , "hostPort": 0
              , "protocol": "udp"
              }
            ]
          , "privileged": false
          , "parameters":
            [ { "key": "a-docker-option", "value": "xxx" }
            , { "key": "b-docker-option", "value": "yyy" }
            ]
          }
        , "volumes":
          [ { "containerPath": "/etc/a"
            , "hostPath": "/var/data/a"
            , "mode": "RO"
            }
          , { "containerPath": "/etc/b"
            , "hostPath": "/var/data/b"
            , "mode": "RW"
            }
          ]
        }
      , "env": { "LD_LIBRARY_PATH": "/usr/local/lib/myLib" }
      , "constraints":
        [ ["attribute-1", "OPERATOR", "value-1"]
        , ["attribute-2", "OPERATOR", "value-2"]
        ]
      , "acceptedResourceRoles": [ "role1", "*" ]
      , "labels": { "environment": "staging" }
      , "fetch":
        [ { "uri": "https://raw.github.com/mesosphere/marathon/master/README.md" }
        , { "uri": "https://foo.com/archive.zip", "executable": false, "extract": true, "cache": true }
        ]
      , "dependencies": ["/product/db/mongo", "/product/db", "../../db"]
      , "healthChecks":
        [ { "protocol": "HTTP"
          , "path": "/health"
          , "gracePeriodSeconds": 3
          , "intervalSeconds": 10
          , "portIndex": 0
          , "timeoutSeconds": 10
          , "maxConsecutiveFailures": 3
          }
        , { "protocol": "HTTPS"
          , "path": "/machinehealth"
          , "gracePeriodSeconds": 3
          , "intervalSeconds": 10
          , "port": 3333
          , "timeoutSeconds": 10
          , "maxConsecutiveFailures": 3
          }
        , { "protocol": "TCP"
          , "gracePeriodSeconds": 3
          , "intervalSeconds": 5
          , "portIndex": 1
          , "timeoutSeconds": 5
          , "maxConsecutiveFailures": 3
          }
        , { "protocol": "COMMAND"
          , "command": { "value": "curl -f -X GET http://$HOST:$PORT0/health" }
          , "maxConsecutiveFailures": 3
          }
        ]
      , "backoffSeconds": 1
      , "backoffFactor": 1.15
      , "maxLaunchDelaySeconds": 3600
      , "taskKillGracePeriodSeconds": 2
      , "upgradeStrategy":
        { "minimumHealthCapacity": 0.5
        , "maximumOverCapacity": 0.2
        }
      , "ipAddress":
        { "groups": [ "backend" ]
        , "labels":
          { "color": "purple"
          , "flavor": "grape"
          , "org": "product"
          , "service": "myApp"
          , "tier": "backend"
          }
        , "networkName": "dev-network"
        }
      }
      """
    )
  }

  behavior of "Cmd"

  it should "create 'cmd' JSON field" in {
    Cmd("foo").json() should matchJsonString(
      """
      {"cmd": "foo"}
      """
    )
  }

  behavior of "Args"

  it should "create 'args' JSON field" in {
    Args(Seq("foo", "bar", "baz")).json() should matchJsonString(
      """
      {"args": ["foo", "bar", "baz"]}
      """
    )
  }

  behavior of "PortDefinition"

  it should "create PortDefinition JSON object" in {
    val portDefinition = PortDefinition(port = 8080, protocol = "tcp")
    portDefinition.json() should matchJsonString(
      """
      { "port": 8080, "protocol": "tcp" }
      """
    )
  }

  it should "create PortDefinition JSON object with name defined" in {
    val portDefinition = PortDefinition(port = 7, protocol = "udp")
      .withName("echo")
    portDefinition.json() should matchJsonString(
      """
      { "port": 7, "protocol": "udp", "name": "echo" }
      """
    )
  }

  it should "create PortDefinition JSON object with label additivity" in {
    val portDefinition = PortDefinition(port = 8080, protocol = "tcp")
      .addLabel("env", "prd")
      .addLabel("loc", "us")
    portDefinition.json() should matchJsonString(
      """
      { "port": 8080
      , "protocol": "tcp"
      , "labels": { "env": "prd", "loc": "us" }
      }
      """
    )
  }

  behavior of "Container.fromJsonString"

  it should "create Container JSON object from raw JSON string" in {
    val givenJsonString =
      """
      { "type": "DOCKER"
      , "volumes":
        [
          { "containerPath": "/etc/a"
          , "hostPath": "/var/data/a"
          , "mode": "RO"
          }
        , { "containerPath": "/etc/b"
          , "hostPath": "/var/data/b"
          , "mode": "RW"
          }
        ]
      }
      """
    val container = Container.fromJsonString(givenJsonString)
    container.json() should matchJsonString(givenJsonString)
  }

  behavior of "DockerContainer"

  behavior of "Constraint"

  it should "create Constraint JSON array" in {
    val constraint = Constraint("attribute", "OPERATOR", "value")
    constraint.json() should matchJsonString("""["attribute", "OPERATOR", "value"]""")
  }

  behavior of "Fetchable"

  it should "create Fetchable JSON object with no values for flags" in {
    val fetchable = Fetchable(uri = "http://example.com")
    fetchable.json() should matchJsonString(
      """
      { "uri": "http://example.com" }
      """
    )
  }

  it should "create Fetchable JSON object, overriding flags" in {
    val fetchable1 = Fetchable(uri = "http://example.com", executable = Some(true))
    fetchable1.json() should matchJsonString(
      """
      { "uri": "http://example.com", "executable": true }
      """
    )
    val fetchable2 = fetchable1.disableExecutable().enableExtract()
    fetchable2.json() should matchJsonString(
      """
      { "uri": "http://example.com", "executable": false, "extract": true }
      """
    )
    val fetchable3 = fetchable2.disableCache()
    fetchable3.json() should matchJsonString(
      """
      { "uri": "http://example.com", "executable": false, "extract": true, "cache": false }
      """
    )
  }

  behavior of "HealthCheck.fromJsonString"

  it should "create HealthCheck JSON object from raw JSON string" in {
    val givenJsonString =
      """
      { "protocol": "COMMAND"
      , "command": "curl -s 'http://example.com/health'"
      }
      """
    val healthCheck = HealthCheck.fromJsonString(givenJsonString)
    healthCheck.json() should matchJsonString(givenJsonString)
  }

  behavior of "HealthCheck.Http"

  it should "create HTTP HealthCheck JSON object" in {
    val healthCheck = HealthCheck.Http(path = "/foo", portIndex = 0)
    healthCheck.json() should matchJsonString(
      """
      { "protocol": "HTTP", "path": "/foo", "portIndex": 0 }
      """
    )
  }

  it should "create HTTPS HealthCheck JSON object" in {
    val healthCheck = HealthCheck.Http(path = "/foo", portIndex = 0, useHttps = true)
    healthCheck.json() should matchJsonString(
      """
      { "protocol": "HTTPS", "path": "/foo", "portIndex": 0 }
      """
    )
  }

  it should "create HTTP HealthCheck JSON object with overridden default fields" in {
    val healthCheck = HealthCheck.Http(path = "/foo", portIndex = 0)
      .copy(gracePeriodSeconds = Some(100))
      .copy(maxConsecutiveFailures = Some(3))
    healthCheck.json() should matchJsonString(
      """
      { "protocol": "HTTP"
      , "path": "/foo"
      , "portIndex": 0
      , "gracePeriodSeconds": 100
      , "maxConsecutiveFailures": 3
      }
      """
    )
  }

  behavior of "HealthCheck.Tcp"

  it should "create TCP HealthCheck JSON objects" in {
    val healthCheck = HealthCheck.Tcp(portIndex = 0)
    healthCheck.json() should matchJsonString(
      """
      { "protocol": "TCP", "portIndex": 0 }
      """
    )
  }

  behavior of "HealthCheck.Command"

  it should "create HealthCheck that invokes arbitrary commands" in {
    val healthCheck = HealthCheck.Command(command = "test 1 -eq 1")
    healthCheck.json() should matchJsonString(
      """
      { "protocol": "COMMAND"
      , "command":
        { "value": "test 1 -eq 1" }
      }
      """
    )
  }

  behavior of "UpgradeStrategy"

  it should "create UpgradeStrategy JSON objects with default property values" in {
    val strategy = UpgradeStrategy()
    strategy.json() should matchJsonString(
      """
      { "upgradeStrategy":
        { "minimumHealthCapacity": 1.0, "maximumOverCapacity": 1.0 }
      }
      """
    )
  }

  it should "create UpgradeStrategy JSON objects with overridden minimumHealthCapacity" in {
    val strategy = UpgradeStrategy(minimumHealthCapacity = 5)
    strategy.json() should matchJsonString(
      """
      { "upgradeStrategy":
        { "minimumHealthCapacity": 5.0, "maximumOverCapacity": 1.0 }
      }
      """
    )
  }

  it should "create UpgradeStrategy JSON objects with overridden maximumOverCapacity" in {
    val strategy = UpgradeStrategy(maximumOverCapacity = 2)
    strategy.json() should matchJsonString(
      """
      { "upgradeStrategy":
        { "minimumHealthCapacity": 1.0, "maximumOverCapacity": 2.0 }
      }
      """
    )
  }

  behavior of "IpAddress"

  it should "create IpAddress JSON objects with networkName defined" in {
    val ipAddress = IpAddress().withNetworkName("foo")
    ipAddress.json() should matchJsonString(
      """
      { "ipAddress":
        { "networkName": "foo" }
      }
      """
    )
  }

  it should "create IpAddress JSON objects with group additivity" in {
    val ipAddress = IpAddress().addGroup("foo").addGroup("bar")
    ipAddress.json() should matchJsonString(
      """
      { "ipAddress":
        { "groups": [ "foo", "bar" ] }
      }
      """
    )
  }

  it should "create IpAddress JSON objects with label additivity" in {
    val ipAddress = IpAddress().addLabel("foo", "bar").addLabel("baz", "qux")
    ipAddress.json() should matchJsonString(
      """
      { "ipAddress":
        { "labels":
          { "foo": "bar", "baz": "qux" }
        }
      }
      """
    )
  }

  it should "create IpAddress JSON objects with all eligible fields" in {
    val ipAddress = IpAddress(
      groups = Some(Seq("group-1")),
      labels = Some(Map("key-1" -> "value-1"))
    ).withNetworkName("foo")
     .addGroup("group-2")
     .addLabel("key-2", "value-2")
    ipAddress.json() should matchJsonString(
      """
      { "ipAddress":
        { "networkName": "foo"
        , "groups": [ "group-1", "group-2" ]
        , "labels": { "key-1": "value-1", "key-2": "value-2" }
        }
      }
      """
    )
  }

  class JValueMatcher(expectedJValue: JValue) extends Matcher[JValue] {
    def apply(givenJValue: JValue) = {
      val Diff(changedJValue, _, removedJValue) = expectedJValue diff givenJValue
      MatchResult(
        changedJValue == JNothing && removedJValue == JNothing,
        pretty(render(expectedJValue)) + "\ndid not match\n" + pretty(render(givenJValue)),
        pretty(render(expectedJValue)) + "\nmatches\n" + pretty(render(givenJValue))
      )
    }
  }

  def matchJsonString(expectedJsonString: String) = new JValueMatcher(parse(expectedJsonString))
}
