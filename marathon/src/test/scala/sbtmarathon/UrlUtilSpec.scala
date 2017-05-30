package sbtmarathon

import java.net.URL
import org.scalatest.{FlatSpec, Matchers}

class UrlUtilSpec extends FlatSpec with Matchers {

  behavior of "UrlUtil"

  it should "preserve all data from an existing URL except protocol" in {
    val parent = new URL("http://example.com")
    val child = UrlUtil.copy(parent, protocol = "https")
    child should be (new URL("https://example.com"))
  }

  it should "preserve all data from an existing URL except user information" in {
    {
      val parent = new URL("http://user:password@example.com")
      val child = UrlUtil.copy(parent, userInfo = "foo:bar")
      child should be (new URL("http://foo:bar@example.com"))
    }
    {
      val parent = new URL("http://user:p%4055w0rd@example.com")
      val child = UrlUtil.copy(parent, userInfo = "user:P@55W0RD")
      child should be (new URL("http://user:P%4055W0RD@example.com"))
    }
    {
      val parent = new URL("http://user:password@example.com")
      val child = UrlUtil.copy(parent, userInfo = "")
      child should be (new URL("http://example.com"))
    }
  }

  it should "preserve all data from an existing URL except host" in {
    val parent = new URL("http://example.com:8080/foo/bar/baz")
    val child = UrlUtil.copy(parent, host = "httpbin.org")
    child should be (new URL("http://httpbin.org:8080/foo/bar/baz"))
  }

  it should "preserve all data from an existing URL except port" in {
    {
      val parent = new URL("http://example.com")
      val child = UrlUtil.copy(parent, port = 8080)
      child should be (new URL("http://example.com:8080"))
    }
    {
      val parent = new URL("https://example.com:443")
      val child = UrlUtil.copy(parent, port = -1)
      child should be (new URL("https://example.com:443"))
      child should be (new URL("https://example.com")) // equivalent to https://example.com:443
    }
    {
      val parent = new URL("http://example.com:8080")
      val child = UrlUtil.copy(parent, port = 80)
      child should be (new URL("http://example.com:80"))
      child should be (new URL("http://example.com")) // equivalent to http://example.com:80
    }
  }

  it should "preserve all data from an existing URL except path" in {
    {
      val parent = new URL("http://example.com/foo/bar/baz.html")
      val child = UrlUtil.copy(parent, path = "/baz/bar/foo.html")
      child should be (new URL("http://example.com/baz/bar/foo.html"))
    }
    {
      val parent = new URL("http://example.com/foo?bar=baz")
      val child = UrlUtil.copy(parent, path = "/qux")
      child should be (new URL("http://example.com/qux?bar=baz"))
    }
    {
      val parent = new URL("http://example.com/foo")
      val child = UrlUtil.copy(parent, path = "")
      child should be (new URL("http://example.com"))
    }
  }

  it should "preserve all data from an existing URL except query string" in {
    {
      val parent = new URL("http://example.com/foo?bar=baz")
      val child = UrlUtil.copy(parent, query = "bar")
      child should be (new URL("http://example.com/foo?bar"))
    }
    {
      val parent = new URL("http://example.com/foo?bar=baz")
      val child = UrlUtil.copy(parent, query = "")
      child should be (new URL("http://example.com/foo"))
    }
  }

  it should "preserve all data from an existing URL except fragment" in {
    {
      val parent = new URL("http://example.com/foo?bar=baz#qux")
      val child = UrlUtil.copy(parent, fragment = "h1")
      child should be (new URL("http://example.com/foo?bar=baz#h1"))
    }
    {
      val parent = new URL("http://example.com/foo#qux")
      val child = UrlUtil.copy(parent, fragment = "")
      child should be (new URL("http://example.com/foo"))
    }
  }
}
