lazy val root = (project in file(".")).enablePlugins(PlayScala)

lazy val scalikejdbcPlayVersion = "2.6.+"
lazy val h2Version = "1.4.+"

name := "Curo"

version := "0.1.0"

scalaVersion := "2.12.8"

maintainer := "Julian Pieles"

resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += Resolver.sonatypeRepo("snapshots")

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"

//********************************************************
// Java - Scala
//********************************************************
val silhouetteV = "5.0.7"
libraryDependencies ++= Seq(
  "io.monix" %% "monix-execution" % "3.0.0",
  "com.typesafe.play" %% "play-json" % "2.8.0",
  "com.typesafe.play" %% "play-json-joda" % "2.8.0",
  "ai.x" %% "play-json-extensions" % "0.42.0",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "org.scalikejdbc" %% "scalikejdbc"                    % "3.4.0",
  "org.scalikejdbc" %% "scalikejdbc-joda-time"          % "3.4.0",
  "org.scalikejdbc" %% "scalikejdbc-config"             % "3.4.0",
  "org.scalikejdbc" %% "scalikejdbc-play-dbapi-adapter" % "2.8.0-scalikejdbc-3.4",
  "org.apache.directory.api" % "api-all" % "1.0.0-RC1",
  "com.mohiva" %% "play-silhouette" % silhouetteV,
  "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteV,
  "com.mohiva" %% "play-silhouette-persistence" % silhouetteV,
  "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteV,
  "net.codingwell" %% "scala-guice" % "4.2.6",
  "com.iheart" %% "ficus" % "1.4.7",
  "com.squareup.okhttp3" % "okhttp" % "3.14.0",
  "com.mohiva" %% "play-silhouette-testkit" % silhouetteV % "test",
  "org.scalatest" %% "scalatest" % "3.1.1" % "test",
  "org.scalikejdbc" %% "scalikejdbc-test"   % "3.4.0"   % "test",
  "ch.vorburger.mariaDB4j" % "mariaDB4j" % "2.2.2" % "test",
  specs2 % Test,
  ehcache,
  filters,
  jdbc,
  evolutions,
  jodaForms,
  guice
)

//********************************************************
// WEBJARS
//********************************************************
libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.8.0",
  "org.webjars.bower" % "jquery" % "3.3.1",
  "org.webjars.bower" % "semantic" % "2.2.14",
  "org.webjars.bower" % "semantic-ui-calendar" % "0.0.8"
)

routesImport += "utils.route.Binders._"

// https://github.com/playframework/twirl/issues/105
TwirlKeys.templateImports := Seq()

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  //"-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
)
