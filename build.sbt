name := "play3-json-form"
version := "1.2.0"

lazy val generator = (project in file("generator"))
  .settings(
    resolvers ++= Resolver.sonatypeOssRepos("public"),
    libraryDependencies += "com.eed3si9n" %% "treehugger" % "0.4.4",
    mainClass := Some("givers.form.generator.Main"),
    publish / skip := true
  )
scalaVersion := "3.3.6"

Test / parallelExecution := false

libraryDependencies ++= Seq(
  "org.playframework" %% "play" % "3.0.8",
  "org.playframework" %% "play-test" % "3.0.8" % Test,
  "org.mockito" % "mockito-core" % "5.18.0" % Test,
  "com.lihaoyi" %% "utest" % "0.9.0" % Test
)
testFrameworks += new TestFramework("utest.runner.Framework")

coverageExcludedPackages := "givers.form.generated.*"

ThisBuild / organization := "io.github.tanin47"
ThisBuild / organizationName := "tanin47"
ThisBuild / organizationHomepage := Some(url("https://github.com/tanin47"))
ThisBuild / homepage := Some(url("https://github.com/tanin47/play-json-form"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/tanin47/play-json-form"),
    "scm:git@github.com:tanin47/play-json-form.git"
  )
)

credentials += Credentials(Path.userHome / ".sbt" / "sonatype_central_credentials")

Test / publishArtifact := false

ThisBuild / publishMavenStyle := true
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}

ThisBuild / licenses := Seq(("MIT", url("http://opensource.org/licenses/MIT")))

ThisBuild / developers := List(
  Developer(
    id = "tanin",
    name = "Tanin Na Nakorn",
    email = "@tanin",
    url = url("https://github.com/tanin47")
  )
)

versionScheme := Some("semver-spec")
