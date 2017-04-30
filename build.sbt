import scalariform.formatter.preferences._

lazy val root = (project in file(".")).enablePlugins(PlayScala)

lazy val scalikejdbcPlayVersion = "2.5.+"
lazy val h2Version = "1.4.+"

name := "Curo"

version := "0.0.3"

scalaVersion := "2.11.8"

resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += Resolver.sonatypeRepo("snapshots")

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"

//********************************************************
// Java - Scala
//********************************************************
libraryDependencies ++= Seq(
  "org.scalikejdbc" %% "scalikejdbc"                    % "2.5.0",
  "org.scalikejdbc" %% "scalikejdbc-config"             % "2.5.0",
  "org.scalikejdbc" %% "scalikejdbc-play-dbapi-adapter" % "2.4.2",
  "org.apache.directory.api" % "api-all" % "1.0.0-RC1",
  "com.mohiva" %% "play-silhouette" % "3.0.0",
  "net.codingwell" %% "scala-guice" % "4.0.0",
  "net.ceedubs" %% "ficus" % "1.1.2",
  "com.mohiva" %% "play-silhouette-testkit" % "3.0.0" % "test",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.scalikejdbc" %% "scalikejdbc-test"   % "2.5.0"   % "test",
  specs2 % Test,
  cache,
  filters,
  jdbc,
  evolutions
)

//********************************************************
// WEBJARS
//********************************************************
libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.4.0",
  "org.webjars.bower" % "semantic-ui" % "2.1.8",
  "org.webjars.bower" % "semantic-ui-calendar" % "0.0.8"
)

routesGenerator := InjectedRoutesGenerator

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen" // Warn when numerics are widened.
)

//********************************************************
// Scalariform settings
//********************************************************

defaultScalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(FormatXml, false)
  .setPreference(DoubleIndentClassDeclaration, false)
  .setPreference(PreserveDanglingCloseParenthesis, true)
