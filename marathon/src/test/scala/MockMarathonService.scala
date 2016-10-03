package sbtmarathon

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.JavaConverters._
import com.twitter.finagle.Service
import com.twitter.finagle.http._
import com.twitter.finagle.http.path._
import com.twitter.finagle.http.service.RoutingService
import com.twitter.util.Future
import org.json4s._
import org.json4s.jackson.JsonMethods._

case class MockMarathonService() extends Service[Request, Response] {

  private implicit val formats = DefaultFormats

  private val applicationInstances = new ConcurrentHashMap[String, AtomicInteger]

  private val underlying = RoutingService.byMethodAndPathObject[Request] {
    case (Method.Post, Root) => startRequestHandler
    case (Method.Delete, Root / applicationId) => destroyRequestHandler(applicationId)
    case (Method.Delete, Root) => systemErrorHandler
  }

  def apply(request: Request): Future[Response] = {
    underlying(request)
  }

  val startRequestHandler = new Service[Request, Response] {
    def apply(request: Request): Future[Response] = {
      val jsonContent = parse(request.contentString)
      val applicationId = (jsonContent \ "id").extract[String]
      val instances = (jsonContent \ "instances").extractOpt[Int].getOrElse(1)
      if (!applicationInstances.containsKey(applicationId)) {
        applicationInstances.put(applicationId, new AtomicInteger(instances))
        val response = Response(request.version, Status.Created)
        Future.value(response)
      } else {
        val response = Response(request.version, Status.BadRequest)
        response.contentType = "application/json"
        response.write(s"""{"message":"An app with id [/$applicationId] already exists."}""")
        Future.value(response)
      }
    }
  }

  def destroyRequestHandler(applicationId: String) = new Service[Request, Response] {
    def apply(request: Request): Future[Response] = {
      val response = Response(request.version, Status.Ok)
      response.contentType = "application/json"
      response.write(s"""{"$applicationId":"destroyed"}""")
      Future.value(response)
    }
  }

  val systemErrorHandler = new Service[Request, Response] {
    def apply(request: Request): Future[Response] = {
      val response = Response(request.version, Status.InternalServerError)
      Future.value(response)
    }
  }
}
