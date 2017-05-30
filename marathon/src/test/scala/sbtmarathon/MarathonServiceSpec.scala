package sbtmarathon

import java.net.URL
import org.scalatest.{FlatSpec, Matchers, EitherValues}

class MarathonServiceSpec extends FlatSpec with Matchers with EitherValues {

  behavior of "MarathonService"

  it should "build URLs that target an application instance" in {
    {
      val url = new URL("http://localhost:8000");
      val service = new MarathonService(url)
      val instanceUrl = service.instanceServiceUrl("foo")
      instanceUrl shouldBe new URL(url, MarathonService.RestApiPath + "/foo")
    }
    {
      val url = new URL("http://localhost:8000/");
      val service = new MarathonService(url)
      val instanceUrl = service.instanceServiceUrl("foo")
      instanceUrl shouldBe new URL(url, MarathonService.RestApiPath + "/foo")
    }
    {
      val url = new URL("http://localhost:8000/foo/bar/");
      val service = new MarathonService(url)
      val instanceUrl = service.instanceServiceUrl("baz")
      instanceUrl shouldBe new URL(url, MarathonService.RestApiPath + "/baz")
    }
    {
      val url = new URL("http://user:password@localhost:8000/foo/bar/");
      val service = new MarathonService(url)
      val instanceUrl = service.instanceServiceUrl("baz")
      println(instanceUrl)
      instanceUrl shouldBe new URL(url, MarathonService.RestApiPath + "/baz")
    }
  }

  it should "build URLs that reference the GUI of an application instance" in {
    {
      val url = new URL(s"http://localhost:8000")
      val service = new MarathonService(url)
      val instanceUrl = service.instanceGuiUrl("foo")
      instanceUrl shouldBe new URL(url, "ui/#/apps/%252Ffoo")
    }
    {
      val url = new URL(s"http://user:password@localhost:8000")
      val service = new MarathonService(url)
      val instanceUrl = service.instanceGuiUrl("foo")
      instanceUrl shouldBe new URL(url, "ui/#/apps/%252Ffoo")
    }
  }

  it should "return a Success result when a 20x response is received" in {
    MockMarathonServer.withServer { server =>
      val url = new URL(s"http://localhost:${server.port}")
      val service = new MarathonService(url)
      val applicationId = "foo"
      val result = service.start(s"""{"id":"$applicationId"}""")
      result.isGood shouldBe true
      val resultE = result.toEither
      resultE.right.value shouldBe a [MarathonService.Success]
    }
  }

  it should "return a UserError result when a 40x response is received" in {
    MockMarathonServer.withServer { server =>
      val url = new URL(s"http://127.0.0.1:${server.port}")
      val service = new MarathonService(url)
      val applicationId = "foo"
      val result1 = service.start(s"""{"id":"$applicationId"}""")
      result1.isGood shouldBe true
      val resultE1 = result1.toEither
      resultE1.right.value shouldBe a [MarathonService.Success]
      val result2 = service.start(s"""{"id":"$applicationId"}""")
      result2.isGood shouldBe true
      val resultE2 = result2.toEither
      resultE2.right.value shouldBe a [MarathonService.UserError]
      resultE2.right.value.message shouldBe Some(s"An app with id [/$applicationId] already exists.")
    }
  }

  it should "return a SystemError result when a 50x response is received" in {
    MockMarathonServer.withServer { server =>
      val url = new URL(s"http://127.0.0.1:${server.port}")
      val service = new MarathonService(url)
      val result = service.destroy("")
      result.isGood shouldBe true
      val resultE = result.toEither
      resultE.right.value shouldBe a [MarathonService.SystemError]
    }
  }
}
