package sbtmarathon

import java.io.IOException
import java.net.{InetSocketAddress, ServerSocket}
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import com.twitter.finagle.Http
import com.twitter.util.Await
import org.slf4j.LoggerFactory

case class MockMarathonServer(port: Int = MockMarathonServer.randomPort) {

  val executor = Executors.newSingleThreadExecutor()

  implicit val executionContext = ExecutionContext.fromExecutor(executor)

  val logger = LoggerFactory.getLogger(getClass)

  val service = MockMarathonService()

  def start(): Unit = {
    val addr = new InetSocketAddress(port)
    val underlying = Http.serve(addr, service)
    logger.info(s"Server listening on $addr")
    Future(Await.ready(underlying))
  }

  def shutdown(): Unit = {
    executor.shutdown()
  }
}

object MockMarathonServer {

  def withServer[A](f: MockMarathonServer => A): A = {
    val server = MockMarathonServer()
    try {
      server.start()
      f(server)
    } finally {
      server.shutdown()
    }
  }

  def randomPort: Int = {
    var server: ServerSocket = null
    try {
      server = new ServerSocket(0)
      server.getLocalPort
    } catch {
      case e: IOException => throw new RuntimeException(e)
    } finally {
      if (server != null) {
        try {
          server.close()
        } catch {
          case _: Exception => /* no-op */
        }
      }
    }
  }
}
