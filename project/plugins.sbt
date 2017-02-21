addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.4.0")

addSbtPlugin("com.eed3si9n" % "sbt-doge" % "0.1.5")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.1")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.4")

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.0.3")

libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
