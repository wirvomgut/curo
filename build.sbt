lazy val root = (project in file(".")).enablePlugins(PlayScala)

name := "Curo"

version := "0.1.1"

scalaVersion := "2.13.1"

maintainer := "Julian Pieles"

resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += Resolver.sonatypeRepo("snapshots")

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"

//********************************************************
// Java - Scala
//********************************************************
val silhouetteV = "7.0.0"
val scalikejdbcV = "3.4.0"
libraryDependencies ++= Seq(
  "io.monix" %% "monix-execution" % "3.0.0",
  "com.typesafe.play" %% "play-json" % "2.8.0",
  "com.typesafe.play" %% "play-json-joda" % "2.8.0",
  "ai.x" %% "play-json-extensions" % "0.42.0",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "org.scalikejdbc" %% "scalikejdbc"                    % scalikejdbcV,
  "org.scalikejdbc" %% "scalikejdbc-joda-time"          % scalikejdbcV,
  "org.scalikejdbc" %% "scalikejdbc-config"             % scalikejdbcV,
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
  "org.scalikejdbc" %% "scalikejdbc-test"   % scalikejdbcV   % "test",
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
)
