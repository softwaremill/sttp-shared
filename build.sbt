import com.softwaremill.SbtSoftwareMillBrowserTestJS._
import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings
import com.softwaremill.Publish.ossPublishSettings
import com.typesafe.tools.mima.core._

val scala2_12 = "2.12.20"
val scala2_13 = "2.13.16"
val scala2 = List(scala2_12, scala2_13)
val scala2alive = List(scala2_12, scala2_13)
val scala3 = List("3.3.7")
val akkaVersion = "2.6.20"
val pekkoVersion = "1.3.0"
val sttpModelVersion = "1.7.17"

val scalaTestVersion = "3.2.19"
val zio1Version = "1.0.18"
val zio2Version = "2.1.22"
val fs2_2_version = "2.5.13"
val fs2_3_version = "3.10.0"
val armeriaVersion = "1.33.4"

excludeLintKeys in Global ++= Set(ideSkipProject)

def dependenciesFor(version: String)(deps: (Option[(Long, Long)] => ModuleID)*): Seq[ModuleID] =
  deps.map(_.apply(CrossVersion.partialVersion(version)))

val commonSettings = commonSmlBuildSettings ++ ossPublishSettings ++ Seq(
  organization := "com.softwaremill.sttp.shared",
  libraryDependencies ++= Seq(
    "org.scalatest" %%% "scalatest" % scalaTestVersion % Test
  ),
  mimaPreviousArtifacts := Set.empty,
  versionScheme := Some("semver-spec")
)

val commonJvmSettings = commonSettings ++ Seq(
  ideSkipProject := (scalaVersion.value != scala2_13),
  bspEnabled := !ideSkipProject.value,
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
    "org.scala-js" %%% "scalajs-dom" % "2.8.1",
    "io.github.cquiroz" %%% "scala-java-time" % "2.6.0" % Test
  )
)

val commonNativeSettings = commonSettings ++ Seq(
  ideSkipProject := true,
  libraryDependencies ++= Seq(
    "io.github.cquiroz" %%% "scala-java-time" % "2.6.0" % Test
  )
)

lazy val allProjectRefs =
  core.projectRefs ++ ws.projectRefs ++ akka.projectRefs ++ pekko.projectRefs ++ armeria.projectRefs ++ fs2ce2.projectRefs ++ fs2.projectRefs ++ monix.projectRefs ++ zio1.projectRefs ++ zio.projectRefs ++ vertx.projectRefs

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
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Nil
        case _            => Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided)
      }
    }
  )
  .jvmPlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonJvmSettings
  )
  .jsPlatform(
    scalaVersions = scala2alive ++ scala3,
    settings = commonJsSettings ++ browserChromeTestSettings
  )
  .nativePlatform(
    scalaVersions = scala2alive ++ scala3,
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
    scalaVersions = scala2alive ++ scala3,
    settings = commonJsSettings ++ browserChromeTestSettings
  )
  .nativePlatform(
    scalaVersions = scala2alive ++ scala3,
    settings = commonNativeSettings
  )
  .dependsOn(core)

lazy val akka = (projectMatrix in file("akka"))
  .settings(
    name := "akka"
  )
  .jvmPlatform(
    scalaVersions = scala2alive ++ scala3,
    settings = commonJvmSettings ++ Seq(
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-stream" % akkaVersion % "provided",
        "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
      )
    )
  )
  .dependsOn(core)

lazy val pekko = (projectMatrix in file("pekko"))
  .settings(
    name := "pekko"
  )
  .jvmPlatform(
    scalaVersions = scala2alive ++ scala3,
    settings = commonJvmSettings ++ Seq(
      libraryDependencies ++= Seq(
        "org.apache.pekko" %% "pekko-stream" % pekkoVersion % "provided",
        "org.apache.pekko" %% "pekko-stream-testkit" % pekkoVersion % Test
      )
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
    libraryDependencies += "co.fs2" %%% "fs2-core" % fs2_2_version
  )
  .jvmPlatform(
    scalaVersions = scala2 ++ scala3,
    settings = commonJvmSettings ++ Seq(
      libraryDependencies += "co.fs2" %% "fs2-io" % fs2_2_version
    )
  )
  .jsPlatform(
    scalaVersions = scala2alive ++ scala3,
    settings = commonJsSettings ++ browserChromeTestSettings
  )
  .dependsOn(core)

lazy val fs2 = (projectMatrix in file("fs2"))
  .settings(name := "fs2", libraryDependencies += "co.fs2" %%% "fs2-core" % fs2_3_version)
  .jvmPlatform(
    scalaVersions = scala2alive ++ scala3,
    settings = commonJvmSettings ++ Seq(
      libraryDependencies ++= Seq(
        "co.fs2" %% "fs2-io" % fs2_3_version,
        "org.scalatest" %% "scalatest" % scalaTestVersion % Test
      )
    )
  )
  .jsPlatform(
    scalaVersions = scala2alive ++ scala3,
    settings = commonJsSettings ++ browserChromeTestSettings
  )
  .dependsOn(core)

lazy val monix = (projectMatrix in file("monix"))
  .settings(
    name := "monix",
    libraryDependencies += "io.monix" %%% "monix" % "3.4.1"
  )
  .jvmPlatform(
    scalaVersions = scala2alive ++ scala3,
    settings = commonJvmSettings
  )
  .jsPlatform(
    scalaVersions = scala2alive ++ scala3,
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
    scalaVersions = scala2alive ++ scala3,
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
    scalaVersions = scala2alive ++ scala3,
    settings = commonJsSettings ++ browserChromeTestSettings
  )
  .nativePlatform(
    scalaVersions = scala2alive ++ scala3,
    settings = commonNativeSettings
  )
  .dependsOn(core)

lazy val vertx = (projectMatrix in file("vertx"))
  .settings(
    name := "vertx"
  )
  .jvmPlatform(
    scalaVersions = List(scala2_12, scala2_13) ++ scala3,
    settings = commonJvmSettings ++ Seq(
      libraryDependencies += "io.vertx" % "vertx-core" % "5.0.5"
    )
  )
  .dependsOn(core)
