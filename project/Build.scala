import sbt._
import sbtassembly.Plugin._
import AssemblyKeys._
import Keys._
import sbtrelease._
import sbtrelease.ReleasePlugin.ReleaseKeys

object BuildSettings {
  lazy val releaseSettings = ReleasePlugin.releaseSettings ++ Seq(
    ReleaseKeys.versionBump := Version.Bump.Minor
  )

  lazy val buildSettings210 = settings(_scalaVersion = "2.10.3", Seq("-unchecked", "-deprecation", "-feature"))

  private def settings(_scalaVersion: String, _scalacOptions: Seq[String]) =
    Defaults.defaultSettings ++
      Seq(
        organization := "com.mediacrossing",
        scalaVersion := _scalaVersion,
        scalacOptions ++= _scalacOptions,
        testOptions in Test := Seq(Tests.Filter(s => s.endsWith("Spec"))))

  lazy val standardSettings = buildSettings210 ++ releaseSettings

  lazy val customAssemblySettings = assemblySettings ++ Seq(
    mergeStrategy in assembly <<= (mergeStrategy in assembly) {
      (old) => {
        case PathList(xs@_*) if xs.last == "cmdline.arg.info.txt.1" =>
          // Fix to deal with some Twitter resource non-sense
          MergeStrategy.filterDistinctLines
        case "play/core/server/ServerWithStop.class" =>
          MergeStrategy.first
        case "logback.xml" =>
          // Rename logback.xml from Play! dependency
          MergeStrategy.rename
        case x =>
          old(x)
      }
    }, artifact in(Compile, assembly) ~= {
      art =>
        art.copy(`classifier` = Some("assembly"))
    }) ++ addArtifact(artifact in(Compile, assembly), assembly)

  object Dependencies {
    val gson = "com.google.code.gson" % "gson" % "2.2.4"
    val poi = "org.apache.poi" % "poi" % "3.10-FINAL"
    val logback = "ch.qos.logback" % "logback-classic" % "1.0.13"
    val playFramework = "play" % "play_2.10" % "2.1.2" exclude(
      "commons-logging", "commons-logging")
    val opencsv = "net.sf.opencsv" % "opencsv" % "2.3"
    val commonsCodec = "commons-codec" % "commons-codec" % "1.8"
    val specs2 = "org.specs2" %% "specs2" % "2.1.1" % "test"
    val dfa = "com.google.apis" % "google-api-services-dfareporting" % "v1.3-rev4-1.17.0-rc"
    val jackson = "com.google.http-client" % "google-http-client-jackson2" % "1.17.0-rc"
    val oauth = "com.google.oauth-client" % "google-oauth-client-jetty" % "1.17.0-rc"
    val guava = "com.google.guava" % "guava" % "14.0"
  }

}

object TargetSegmentingBuild extends Build {

  import BuildSettings._
  import Dependencies._

  val root = Project(
    id = "toledo",
    base = file("."),
    settings =
      standardSettings ++
        Seq(
          libraryDependencies ++=
            Seq(
              gson,
              poi,
              logback,
              playFramework,
              opencsv,
              commonsCodec,
              specs2,
              dfa,
              jackson,
              oauth,
              guava
          )) ++
        customAssemblySettings
  )
}

