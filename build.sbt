lazy val root = (project in file(".")).enablePlugins(PlayScala)

name := "Curo"

version := "0.1.1"

scalaVersion := "2.13.13"

maintainer := "Julian Pieles"

resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += Resolver.sonatypeRepo("snapshots")

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"

//********************************************************
// Java - Scala
//********************************************************
val playV = "3.0.2"
val silhouetteV = "10.0.0"
val scalikejdbcV = "4.2.0"

dependencyOverrides ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
)
libraryDependencies ++= Seq(
  "io.monix" %% "monix-execution" % "3.0.0",
  "org.playframework" %% "play-json" % playV,
  "org.playframework" %% "play-json-joda" % playV,
  "ai.x" %% "play-json-extensions" % "0.42.0",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "org.scalikejdbc" %% "scalikejdbc"                    % scalikejdbcV,
  "org.scalikejdbc" %% "scalikejdbc-joda-time"          % scalikejdbcV,
  "org.scalikejdbc" %% "scalikejdbc-config"             % scalikejdbcV,
  "org.scalikejdbc" %% "scalikejdbc-play-dbapi-adapter" % "3.0.0-scalikejdbc-4.2",
  "org.apache.directory.api" % "api-all" % "1.0.0-RC1",
  "org.playframework.silhouette" %% "play-silhouette" % silhouetteV,
  "org.playframework.silhouette" %% "play-silhouette-password-bcrypt" % silhouetteV,
  "org.playframework.silhouette" %% "play-silhouette-persistence" % silhouetteV,
  "org.playframework.silhouette" %% "play-silhouette-crypto-jca" % silhouetteV,
  "net.codingwell" %% "scala-guice" % "4.2.6",
  "com.iheart" %% "ficus" % "1.4.7",
  "com.squareup.okhttp3" % "okhttp" % "3.14.0",
  "org.playframework.silhouette" %% "play-silhouette-testkit" % silhouetteV % "test",
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
  "org.webjars" %% "webjars-play" % "3.0.1",
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
  //"-quickfix:any",
)

