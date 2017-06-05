package sbtmarathon

import java.net.URL
import org.scalatest.{FlatSpec, Matchers, EitherValues}

class MockAuthenticatingMarathonServerSpec extends FlatSpec with Matchers with EitherValues {

  import MockAuthenticatingMarathonServer._

  behavior of "MockAuthenticatingMarathonServer"

  it should "accept requests for that contain valid credentials" in {
    withServer("user" -> "password") { server =>
      val url = new URL(s"http://user:password@127.0.0.1:${server.port}")
      val service = new MarathonService(url)
      val applicationId = "foo"
      val result1 = service.start(s"""{"id":"$applicationId"}""")
      result1.isGood shouldBe true
      val resultE1 = result1.toEither
      resultE1.right.value shouldBe a [MarathonService.Success]
      val result2 = service.destroy(applicationId)
      result2.isGood shouldBe true
      val resultE2 = result2.toEither
      resultE2.right.value shouldBe a [MarathonService.Success]
    }
  }

  it should "deny requests with invalid credentials" in {
    withServer("user" -> "password") { server =>
      val url = new URL(s"http://foo:bar@127.0.0.1:${server.port}")
      val service = new MarathonService(url)
      val result = service.start(s"""{"id":"foo"}""")
      result.isGood shouldBe true
      val resultE = result.toEither
      resultE.right.value shouldBe a [MarathonService.UserError]
    }
  }

  it should "deny requests with non-existent credentials" in {
    withServer("user" -> "password") { server =>
      val url = new URL(s"http://127.0.0.1:${server.port}")
      val service = new MarathonService(url)
      val result = service.start(s"""{"id":"foo"}""")
      result.isGood shouldBe true
      val resultE = result.toEither
      resultE.right.value shouldBe a [MarathonService.UserError]
    }
  }
}
