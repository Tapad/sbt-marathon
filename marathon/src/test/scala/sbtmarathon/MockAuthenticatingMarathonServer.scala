package sbtmarathon

import scala.util.Try
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.{Base64StringEncoder, Future}
import org.scalactic.{Or, Good, Bad, Validation, Pass, Fail}

class MockAuthenticatingMarathonServer(
  credentials: Map[String, String],
  port: Int = MockMarathonServer.randomPort,
  service: Service[Request, Response] = MockMarathonService()
) extends MockMarathonServer(
  port,
  MockAuthenticatingMarathonServer.buildAuthenticationFilter(credentials) andThen service
)

object MockAuthenticatingMarathonServer {

  def withServer[A](credentials: (String, String)*)(f: MockMarathonServer => A): A = {
    val server = new MockAuthenticatingMarathonServer(credentials.toMap)
    try {
      server.start()
      f(server)
    } finally {
      server.shutdown()
    }
  }

  def buildAuthenticationFilter(credentials: Map[String, String]) = new SimpleFilter[Request, Response] {
    def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
      (for {
        authorizationHeader <- Or.from[String, Throwable](request.authorization, orElse = UserError)
        userAndPassword <- extractCredentials(authorizationHeader)
        (user, password) = userAndPassword if isAuthenticated(user, password)
      } yield {
        request
      }) match {
        case Good(request) => service(request)
        case Bad(_) => Future.value(Response(Status.Unauthorized))
      }
    }

    private def extractCredentials(authorizationHeader: String): (String, String) Or Throwable = {
      val result = Try {
        val headerPattern = "Basic (.*)".r
        val headerPattern(encodedCredentials) = authorizationHeader
        val decodedCredentials = new String(Base64StringEncoder.decode(encodedCredentials), "UTF-8")
        val credentialsPattern = "(.*):(.*)".r
        val credentialsPattern(user, password) = decodedCredentials
        user -> password
      }
      Or.from[(String, String)](result)
    }

    private def isAuthenticated(user: String, password: String): Validation[Throwable] = {
      if (credentials.contains(user) && credentials(user) == password) Pass else Fail(UserError)
    }
  }

  private case object UserError extends Throwable
}
