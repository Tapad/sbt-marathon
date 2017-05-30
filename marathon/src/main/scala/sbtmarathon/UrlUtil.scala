package sbtmarathon

import java.net.{URI, URL}

object UrlUtil {

  def copy(
    parent: URL,
    protocol: String = null,
    userInfo: String = null,
    host: String = null,
    port: Int = -1,
    path: String = null,
    query: String = null,
    fragment: String = null
  ): URL = {
    new URI(
      if (protocol != null) protocol else parent.getProtocol,
      if (userInfo == "") null else if (userInfo != null) userInfo else parent.getUserInfo,
      if (host != null) host else parent.getHost,
      if (port != -1) port else parent.getPort,
      if (path == "") null else if (path != null) path else parent.getPath,
      if (query == "") null else if (query != null) query else parent.getQuery,
      if (fragment == "") null else if (fragment != null) fragment else parent.getRef
    ).toURL
  }
}
