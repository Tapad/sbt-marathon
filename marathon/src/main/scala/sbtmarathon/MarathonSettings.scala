package sbtmarathon

import java.net.URL
import sbt._
import sbt.Keys._
import sbt.complete.DefaultParsers._
import org.scalactic.{Or, Good, Bad}
import com.tapad.util.Version

object MarathonSettings {

  import MarathonKeys._

  lazy val projectSettings = requiredSettings ++ defaultSettings

  lazy val requiredSettings = Seq(
    dockerRegistry := {
      sys.error("The docker registry is not defined. Please declare a value for the `dockerRegistry` setting.")
    },
    marathonServiceUrl := {
      sys.error("A Marathon service URL is not defined. Please declare a value for the `marathonServiceUrl` setting.")
    },
    marathonServiceRequest := {
      sys.error("A Marathon service request entity is not defined. Please declare a value for the `marathonServiceRequest` setting.")
    }
  )

  lazy val defaultSettings = Seq(
    commands += setMarathonServiceUrl,
    marathonApplicationId := {
      val parsedVersion = Version(version.value)
      val sanitizedName = sanitizeApplicationIdToken(name.value)
      val sanitizedVersion = sanitizeApplicationIdToken(
        (new StringBuilder)
          .append(parsedVersion.major)
          .append('.')
          .append(parsedVersion.minor)
          .append('.')
          .append(parsedVersion.release)
          .append(parsedVersion.prerelease.fold("")("-" + _))
          .toString
      )
      s"$sanitizedName/v$sanitizedVersion"
    },
    marathonService := {
      try {
        val url = new URL(marathonServiceUrl.value)
        new MarathonService(url)
      } catch {
        case e: Exception => sys.error(
          "Unable to instantiate the Marathon service interface: " +
          ErrorHandling.reducedToString(e)
        )
      }
    },
    marathonServiceStart := {
      val log = streams.value.log
      val service = marathonService.value
      val requestEntity = marathonServiceRequest.value
      service.start(requestEntity) match {
        case Good(MarathonService.Success(_)) =>
          log.info("Application successfully started")
          val applicationId = marathonApplicationId.value
          val url = service.instanceGuiUrl(applicationId)
          log.info(s"Open $url in your browser to administer the application")
        case Good(result) if result.message.nonEmpty =>
          sys.error(s"Unable to start application: ${result.message.get}")
        case Good(_) =>
          sys.error(s"Unable to start application")
        case Bad(e) =>
          sys.error(
            "Unexpected error encountered when attempting to start the application: " +
            ErrorHandling.reducedToString(e)
          )
      }
    },
    marathonServiceDestroy := {
      val log = streams.value.log
      val service = marathonService.value
      val applicationId = marathonApplicationId.value
      service.destroy(applicationId) match {
        case Good(MarathonService.Success(_)) =>
          log.info("Application successfully destroyed")
        case Good(result) if result.message.nonEmpty =>
          sys.error(s"Unable to destroy application $applicationId: ${result.message.get}")
        case Good(result) =>
          sys.error(s"Unable to destroy application $applicationId")
        case Bad(e) =>
          sys.error(
            s"Unexpected error encountered when attempting to destroy application $applicationId: " +
            ErrorHandling.reducedToString(e)
          )
      }
    },
    marathonServiceUpdate := {
      val log = streams.value.log
      val service = marathonService.value
      val applicationId = marathonApplicationId.value
      val requestEntity = marathonServiceRequest.value
      service.update(applicationId, requestEntity) match {
        case Good(MarathonService.Success(_)) =>
          log.info("Application successfully updated")
          val applicationId = marathonApplicationId.value
          val url = service.instanceGuiUrl(applicationId)
          log.info(s"Open $url in your browser to administer the application")
        case Good(result) if result.message.nonEmpty =>
          sys.error(s"Unable to update application $applicationId: ${result.message.get}")
        case Good(result) =>
          sys.error(s"Unable to update application $applicationId")
        case Bad(e) =>
          sys.error(
            s"Unexpected error encountered when attempting to update application $applicationId: " +
            ErrorHandling.reducedToString(e)
          )
      }
    },
    marathonServiceRestart := {
      val log = streams.value.log
      val service = marathonService.value
      val applicationId = marathonApplicationId.value
      service.restart(applicationId) match {
        case Good(MarathonService.Success(_)) =>
          log.info("Application successfully restarted")
          val applicationId = marathonApplicationId.value
          val url = service.instanceGuiUrl(applicationId)
          log.info(s"Open $url in your browser to administer the application")
        case Good(result) if result.message.nonEmpty =>
          sys.error(s"Unable to restart application $applicationId: ${result.message.get}")
        case Good(result) =>
          sys.error(s"Unable to restart application $applicationId")
        case Bad(e) =>
          sys.error(
            s"Unexpected error encountered when attempting to restart application $applicationId: " +
            ErrorHandling.reducedToString(e)
          )
      }
    },
    marathonServiceScale := {
      val log = streams.value.log
      val service = marathonService.value
      val applicationId = marathonApplicationId.value
      val numInstances = (Space ~> token(NatBasic)).parsed
      service.scale(applicationId, numInstances) match {
        case Good(MarathonService.Success(_)) =>
          log.info(s"Application successfully scaled to $numInstances instance(s)")
          if (numInstances > 0) {
            val applicationId = marathonApplicationId.value
            val url = service.instanceGuiUrl(applicationId)
            log.info(s"Open $url in your browser to administer the application")
          }
        case Good(result) if result.message.nonEmpty =>
          sys.error(s"Unable to scale application $applicationId: ${result.message.get}")
        case Good(result) =>
          sys.error(s"Unable to scale application $applicationId")
        case Bad(e) =>
          sys.error(
            s"Unexpected error encountered when attempting to destroy application $applicationId: " +
            ErrorHandling.reducedToString(e)
          )
      }
    }
  )

  def setMarathonServiceUrl = Command.single("marathonSetServiceUrl") { (state, url) =>
    Project.extract(state).append(
      Seq(
        marathonServiceUrl := url
      ),
      state
    )
  }

  def sanitizeApplicationIdToken(token: String): String = token
    .replaceAll("[^a-zA-Z0-9\\-\\.]", "-") // replace invalid characters with dashes
    .replaceAll("^-+", "")                 // drop leading dashes
    .replaceAll("-+$", "")                 // drop trailing dashes
    .toLowerCase                           // convert to lowercase
}
