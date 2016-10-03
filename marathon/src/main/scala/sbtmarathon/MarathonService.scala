package sbtmarathon

import java.net.{URL, URLEncoder, InetSocketAddress}
import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import com.twitter.finagle.{Http, Name, Address}
import com.twitter.finagle.http.{RequestBuilder, Request, Response}
import com.twitter.io.Buf
import org.scalactic.{Or, Good, Bad}

class MarathonService(url: URL) {

  import MarathonService._

  val port = if (url.getPort < 0) url.getDefaultPort else url.getPort

  def start(jsonString: String): Result Or Throwable = {
    val request = RequestBuilder()
      .url(url)
      .setHeader("Content-type", JsonContentType)
      .buildPost(jsonString)
    executeRequest(request)
  }

  def destroy(applicationId: String): Result Or Throwable = {
    val url = instanceServiceUrl(applicationId)
    val request = RequestBuilder()
      .url(url)
      .buildDelete()
    executeRequest(request, url = url)
  }

  def update(applicationId: String, jsonString: String): Result Or Throwable = {
    val url = instanceServiceUrl(applicationId)
    val request = RequestBuilder()
      .url(url)
      .setHeader("Content-type", JsonContentType)
      .buildPut(jsonString)
    executeRequest(request, url)
  }

  def restart(applicationId: String): Result Or Throwable = {
    val url = instanceServiceUrl(applicationId)
    val request = RequestBuilder()
      .url(url)
      .setHeader("Content-type", JsonContentType)
      .buildPost(Buf.Empty)
    executeRequest(request, url)
  }

  def scale(applicationId: String, numInstances: Int): Result Or Throwable = {
    val url = instanceServiceUrl(applicationId)
    val jsonString = s"""{"instances":$numInstances}"""
    val request = RequestBuilder()
      .url(url)
      .setHeader("Content-type", JsonContentType)
      .buildPut(jsonString)
    executeRequest(request, url)
  }

  def executeRequest(request: Request, url: URL = this.url): Result Or Throwable = {
    val port = if (url.getPort < 0) url.getDefaultPort else url.getPort
    val addr = Address(new InetSocketAddress(url.getHost, port))
    val service = Http.newService(Name.bound(addr), "")
    val response = service(request).ensure { service.close() }
    val promise = Promise[Response]
    response.onSuccess(promise.success _)
    response.onFailure(promise.failure _)
    val future = promise.future.map { response =>
      val responseString = response.contentString
      val result = response.statusCode match {
        case n if n >= 200 && n < 400 => Success(responseString)
        case n if n >= 400 && n < 500 => UserError(responseString)
        case n if n >= 500            => SystemError(responseString)
      }
      Good(result)
    }
    try {
      Await.result(future, Duration.Inf)
    } catch {
      case e: Exception => Bad(e)
    }
  }

  def instanceServiceUrl(applicationId: String): URL = {
    new URL(url.getProtocol, url.getHost, port, url.getFile + s"/$applicationId")
  }

  def instanceGuiUrl(applicationId: String): URL = {
    val encodedApplicationId = URLEncoder.encode(s"/$applicationId", "UTF-8")
    val path =  s"/ui/#/apps/$encodedApplicationId"
    new URL(url.getProtocol, url.getHost, port, path)
  }
}

object MarathonService {

  sealed trait Result { def responseString: String }
  case class Success(responseString: String) extends Result
  case class UserError(responseString: String) extends Result
  case class SystemError(responseString: String) extends Result

  val JsonContentType = "application/json"

  implicit def jsonStringToBuf(jsonString: String): Buf = {
    val jsonBytes = jsonString.getBytes("UTF-8")
    Buf.ByteArray(jsonBytes: _*)
  }
}
