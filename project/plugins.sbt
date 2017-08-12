addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

addSbtPlugin("com.eed3si9n" % "sbt-doge" % "0.1.5")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.3.3")

libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
