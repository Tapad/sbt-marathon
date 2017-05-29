package sbtmarathon

import java.net.URL
import com.twitter.finagle.http.{RequestBuilder, Request}
import org.scalatest.{FlatSpec, Matchers, EitherValues}

class MarathonServiceFunctionalSpec extends FlatSpec with Matchers with EitherValues {

  behavior of "MarathonService"

  it should "execute requests against HTTP URLs with no explicit port specified" taggedAs (FunctionalTest) in {
    val url = new URL("http://example.com")
    val service = new MarathonService(url)
    val request = getRequest(url)
    val result = service.executeRequest(request, url)
    result.isGood shouldBe true
    val resultE = result.toEither
    resultE.right.value shouldBe a [MarathonService.Success]
  }

  it should "execute requests against HTTP URLs with an explicit port specified" taggedAs (FunctionalTest) in {
    val url = new URL("http://example.com:80")
    val service = new MarathonService(url)
    val request = getRequest(url)
    val result = service.executeRequest(request, url)
    result.isGood shouldBe true
    val resultE = result.toEither
    resultE.right.value shouldBe a [MarathonService.Success]
  }

  it should "execute requests against HTTPS URLs with no explicit port specified" taggedAs (FunctionalTest) in {
    val url = new URL("https://example.com")
    val service = new MarathonService(url)
    val request = getRequest(url)
    val result = service.executeRequest(request, url)
    result.isGood shouldBe true
    val resultE = result.toEither
    resultE.right.value shouldBe a [MarathonService.Success]
  }

  it should "execute requests against HTTPS URLs with an explicit port specified" taggedAs (FunctionalTest) in {
    val url = new URL("https://example.com:443")
    val service = new MarathonService(url)
    val request = getRequest(url)
    val result = service.executeRequest(request, url)
    result.isGood shouldBe true
    val resultE = result.toEither
    resultE.right.value shouldBe a [MarathonService.Success]
  }

  it should "execute requests against HTTP URLs that include basic authentication credentials" taggedAs (FunctionalTest) in {
    val url = new URL("http://user:password@httpbin.org/basic-auth/user/password")
    val service = new MarathonService(url)
    val request = getRequest(url)
    val result = service.executeRequest(request, url)
    result.isGood shouldBe true
    val resultE = result.toEither
    resultE.right.value shouldBe a [MarathonService.Success]
  }

  it should "execute requests against HTTPS URLs that include basic authentication credentials" taggedAs (FunctionalTest) in {
    val url = new URL("https://user:password@httpbin.org/basic-auth/user/password")
    val service = new MarathonService(url)
    val request = getRequest(url)
    val result = service.executeRequest(request, url)
    result.isGood shouldBe true
    val resultE = result.toEither
    resultE.right.value shouldBe a [MarathonService.Success]
  }

  def getRequest(url: URL): Request = {
    RequestBuilder().url(url).buildGet()
  }
}
