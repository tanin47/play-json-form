name := "play-json-form"
version := "1.1.2"

lazy val generator = (project in file("generator"))
  .settings(
    resolvers ++= Resolver.sonatypeOssRepos("public"),
    libraryDependencies += "com.eed3si9n" %% "treehugger" % "0.4.3",
    mainClass := Some("givers.form.generator.Main"),
    publish / skip := true
  )
scalaVersion := "2.13.16"

Test / parallelExecution := false

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.9.7",
  "com.typesafe.play" %% "play-test" % "2.9.7" % Test,
  "org.mockito" % "mockito-core" % "2.18.3" % Test,
  "com.lihaoyi" %% "utest" % "0.7.2" % Test
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
