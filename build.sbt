import com.softwaremill.SbtSoftwareMillBrowserTestJS._
import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings
import com.softwaremill.Publish.ossPublishSettings
import com.typesafe.tools.mima.core._

val scala2_11 = "2.11.12"
val scala2_12 = "2.12.17"
val scala2_13 = "2.13.10"
val scala2 = List(scala2_11, scala2_12, scala2_13)
val scala3 = List("3.2.0")

val sttpModelVersion = "1.5.2"

val scalaTestVersion = "3.2.14"
val zio1Version = "1.0.17"
val zio2Version = "2.0.2"
val fs2_2_version: Option[(Long, Long)] => String = {
  case Some((2, 11)) => "2.1.0"
  case _             => "2.5.9"
}
val fs2_3_version = "3.3.0"
val armeriaVersion = "1.20.1"

excludeLintKeys in Global ++= Set(ideSkipProject)

def dependenciesFor(version: String)(deps: (Option[(Long, Long)] => ModuleID)*): Seq[ModuleID] =
  deps.map(_.apply(CrossVersion.partialVersion(version)))

val commonSettings = commonSmlBuildSettings ++ ossPublishSettings ++ Seq(
  organization := "com.softwaremill.sttp.shared",
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test
  ),
  mimaPreviousArtifacts := Set.empty,
  versionScheme := Some("semver-spec")
)

val commonJvmSettings = commonSettings ++ Seq(
  scalacOptions ++= Seq("-target:jvm-1.8"),
  ideSkipProject := (scalaVersion.value != scala2_13),
  mimaPreviousArtifacts := previousStableVersion.value.map(organization.value %% moduleName.value % _).toSet,
  mimaReportBinaryIssues := { if ((publish / skip).value) {} else mimaReportBinaryIssues.value }
)

val commonJsSettings = commonSettings ++ Seq(
  ideSkipProject := true,
  Compile / scalacOptions ++= {
    if (isSnapshot.value) Seq.empty
    else {
      val mapSourcePrefix =
        if (ScalaArtifacts.isScala3(scalaVersion.value))
          "-scalajs-mapSourceURI"
        else
          "-P:scalajs:mapSourceURI"

      Seq {
        val dir = project.base.toURI.toString.replaceFirst("[^/]+/?$", "")
        val url = "https://raw.githubusercontent.com/softwaremill/sttp-shared"
        s"$mapSourcePrefix:$dir->$url/v${version.value}/"
      }
    }
  },
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "2.3.0"
  )
)

val commonNativeSettings = commonSettings ++ Seq(
  nativeLinkStubs := true,
  ideSkipProject := true,
  libraryDependencies ++= Seq(
    "org.scala-native" %%% "test-interface" % nativeVersion
  )
)

lazy val allProjectRefs =
  core.projectRefs ++ ws.projectRefs ++ akka.projectRefs ++ armeria.projectRefs ++ fs2ce2.projectRefs ++ fs2.projectRefs ++ monix.projectRefs ++ zio1.projectRefs ++ zio.projectRefs

lazy val projectAggregates: Seq[ProjectReference] = if (sys.env.isDefinedAt("STTP_NATIVE")) {
  println("[info] STTP_NATIVE defined, including sttp-native in the aggregate projects")
  allProjectRefs
} else {
  println("[info] STTP_NATIVE *not* defined, *not* including sttp-native in the aggregate projects")
  allProjectRefs.filterNot(_.toString.contains("Native"))
}

val compileAndTest = "compile->compile;test->test"

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(publish / skip := true, name := "sttp-shared", scalaVersion := scala2_13)
  .aggregate(projectAggregates: _*)

lazy val core = (projectMatrix in file("core"))
  .settings(
    name := "core",
    mimaBinaryIssueFilters ++= {
      if (scalaVersion.value == scala2_11) {
        // excluding this for 2.11 as the `blocking` method will only ever be called in recompiled library code
        Seq(ProblemFilters.exclude[ReversedMissingMethodProblem]("sttp.monad.MonadError.blocking"))
      } else Nil
    }
  )
  .jvmPlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonJvmSettings
  )
  .jsPlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonJsSettings ++ browserChromeTestSettings
  )
  .nativePlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonNativeSettings
  )

lazy val ws = (projectMatrix in file("ws"))
  .settings(
    name := "ws",
    libraryDependencies += "com.softwaremill.sttp.model" %%% "core" % sttpModelVersion
  )
  .jvmPlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonJvmSettings
  )
  .jsPlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonJsSettings ++ browserChromeTestSettings
  )
  .nativePlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonNativeSettings
  )
  .dependsOn(core)

lazy val akka = (projectMatrix in file("akka"))
  .settings(
    name := "akka"
  )
  .jvmPlatform(
    scalaVersions = List(scala2_12, scala2_13) ++ scala3,
    settings = commonJvmSettings ++ Seq(
      libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.20" % "provided"
    )
  )
  .dependsOn(core)

lazy val armeria = (projectMatrix in file("armeria"))
  .settings(
    name := "armeria"
  )
  .jvmPlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonJvmSettings ++ Seq(
      libraryDependencies += "com.linecorp.armeria" % "armeria" % armeriaVersion
    )
  )
  .dependsOn(core)

lazy val fs2ce2 = (projectMatrix in file("fs2-ce2"))
  .settings(
    name := "fs2-ce2",
    libraryDependencies ++= dependenciesFor(scalaVersion.value)(
      "co.fs2" %%% "fs2-core" % fs2_2_version(_)
    )
  )
  .jvmPlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonJvmSettings ++ Seq(
      libraryDependencies ++= dependenciesFor(scalaVersion.value)(
        "co.fs2" %% "fs2-io" % fs2_2_version(_)
      )
    )
  )
  .jsPlatform(
    scalaVersions = List(scala2_12, scala2_13) ++ scala3,
    settings = commonJsSettings ++ browserChromeTestSettings
  )
  .dependsOn(core)

lazy val fs2 = (projectMatrix in file("fs2"))
  .settings(name := "fs2", libraryDependencies += "co.fs2" %%% "fs2-core" % fs2_3_version)
  .jvmPlatform(
    scalaVersions = List(scala2_12, scala2_13) ++ scala3,
    settings = commonJvmSettings ++ Seq(
      libraryDependencies += "co.fs2" %% "fs2-io" % fs2_3_version
    )
  )
  .jsPlatform(
    scalaVersions = List(scala2_12, scala2_13) ++ scala3,
    settings = commonJsSettings ++ browserChromeTestSettings
  )
  .nativePlatform(
    scalaVersions = List(scala2_12, scala2_13) ++ scala3,
    settings = commonNativeSettings
  )
  .dependsOn(core)

lazy val monix = (projectMatrix in file("monix"))
  .settings(
    name := "monix",
    libraryDependencies += "io.monix" %%% "monix" % "3.4.1"
  )
  .jvmPlatform(
    scalaVersions = List(scala2_12, scala2_13) ++ scala3,
    settings = commonJvmSettings
  )
  .jsPlatform(
    scalaVersions = List(scala2_12, scala2_13) ++ scala3,
    settings = commonJsSettings ++ browserChromeTestSettings
  )
  .dependsOn(core)

lazy val zio1 = (projectMatrix in file("zio1"))
  .settings(
    name := "zio1",
    libraryDependencies ++= Seq("dev.zio" %%% "zio-streams" % zio1Version, "dev.zio" %%% "zio" % zio1Version)
  )
  .jvmPlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonJvmSettings
  )
  .jsPlatform(
    scalaVersions = List(scala2_12, scala2_13) ++ scala3,
    settings = commonJsSettings ++ browserChromeTestSettings
  )
  .dependsOn(core)

lazy val zio = (projectMatrix in file("zio"))
  .settings(
    name := "zio",
    libraryDependencies ++= Seq("dev.zio" %%% "zio-streams" % zio2Version, "dev.zio" %%% "zio" % zio2Version)
  )
  .jvmPlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonJvmSettings
  )
  .jsPlatform(
    scalaVersions = List(scala2_12, scala2_13) ++ scala3,
    settings = commonJsSettings ++ browserChromeTestSettings
  )
  .nativePlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonNativeSettings
  )
  .dependsOn(core)
