name := "Curo"

resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += Resolver.sonatypeRepo("snapshots")

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"

lazy val commonSettings = Seq(
  scalaVersion  := "2.12.5",
  organization  := "de.wirvomgut",
  version       := "0.1.0"
)

lazy val server = (project in file("server")).settings(commonSettings).settings(
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  //********************************************************
  // Java - Scala
  //********************************************************
  libraryDependencies ++= Seq(
    "com.vmunier" %% "scalajs-scripts" % "1.1.2",
    "com.typesafe.play" %% "play-json" % "2.6.9",
    "com.typesafe.play" %% "play-json-joda" % "2.6.9",
    "mysql" % "mysql-connector-java" % "5.1.36",
    "org.scalikejdbc" %% "scalikejdbc"                    % "3.2.2",
    "org.scalikejdbc" %% "scalikejdbc-joda-time"          % "3.2.2",
    "org.scalikejdbc" %% "scalikejdbc-config"             % "3.2.2",
    "org.scalikejdbc" %% "scalikejdbc-play-dbapi-adapter" % "2.6.0-scalikejdbc-3.2",
    "org.apache.directory.api" % "api-all" % "1.0.0-RC1",
    "com.mohiva" %% "play-silhouette" % "5.0.5",
    "com.mohiva" %% "play-silhouette-password-bcrypt" % "5.0.5",
    "com.mohiva" %% "play-silhouette-persistence" % "5.0.5",
    "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.5",
    "net.codingwell" %% "scala-guice" % "4.2.1",
    "com.iheart" %% "ficus" % "1.4.3",
    "com.mohiva" %% "play-silhouette-testkit" % "5.0.0" % "test",
    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
    "org.scalikejdbc" %% "scalikejdbc-test"   % "2.5.0"   % "test",
    "ch.vorburger.mariaDB4j" % "mariaDB4j" % "2.2.2" % "test",
    specs2 % Test,
    ehcache,
    filters,
    jdbc,
    evolutions,
    jodaForms,
    guice
  ),
  //********************************************************
  // WEBJARS
  //********************************************************
  libraryDependencies ++= Seq(
    "org.webjars" %% "webjars-play" % "2.6.3",
    "org.webjars.bower" % "jquery" % "3.3.1",
    "org.webjars.bower" % "semantic" % "2.2.14",
    "org.webjars.bower" % "semantic-ui-calendar" % "0.0.8"
    //"org.webjars.npm" % "bulma" % "0.5.2"
  ),
  routesImport += "utils.route.Binders._"
).enablePlugins(PlayScala).
  dependsOn(sharedJvm)

lazy val client = (project in file("client")).settings(commonSettings).settings(
  scalaJSUseMainModuleInitializer := false,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.5"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(commonSettings)
lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js
