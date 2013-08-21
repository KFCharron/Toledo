import sbt._
import sbtassembly.Plugin._
import AssemblyKeys._
import Keys._

object BuildSettings {

  val buildSettings210 = settings(_scalaVersion = "2.10.1", Seq("-unchecked", "-deprecation", "-feature"))

  private def settings(_scalaVersion: String, _scalacOptions: Seq[String]) =
    Defaults.defaultSettings ++
      Seq(
        organization := "com.mediacrossing",
        scalaVersion := _scalaVersion,
        scalacOptions ++= _scalacOptions,
        testOptions in Test := Seq(Tests.Filter(s => s.endsWith("Spec"))))

  lazy val customAssemblySettings = assemblySettings ++ Seq(
    mergeStrategy in assembly <<= (mergeStrategy in assembly) {
      (old) => {
        //Fix to deal with some Twitter resource non-sense
        case PathList(xs@_*) if (xs.last == "cmdline.arg.info.txt.1") => MergeStrategy.filterDistinctLines

        case x => old(x)
      }
    }, artifact in(Compile, assembly) ~= {
      art =>
        art.copy(`classifier` = Some("assembly"))
    }) ++ addArtifact(artifact in(Compile, assembly), assembly)

  object Dependencies {
    val gson = "com.google.code.gson" % "gson" % "2.2.4"
    val javacsv = "net.sourceforge.javacsv" % "javacsv" % "2.0"
  }

}

object TargetSegmentingBuild extends Build {

  import BuildSettings._
  import Dependencies._

  val root = Project(
    id = "target-segmenting",
    base = file("."),
    settings = buildSettings210 ++ Seq(libraryDependencies ++= Seq(gson, javacsv))
  )
}

