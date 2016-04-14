import sbt._
import bintray.BintrayKeys._
import com.typesafe.sbt.pgp.PgpKeys
import sbt.Keys._
import sbtrelease.ReleasePlugin.autoImport._

object Settings {
  lazy val commons = Seq(
    scalaVersion := "2.11.8",
    organization := "me.maciejb.etcd-client",
    description := "etc.d client",
    homepage := Some(url("https://github.com/maciej/etcd-client")),
    startYear := Some(2016),
    licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
  )

  lazy val release = Seq(
    isSnapshot <<= isSnapshot or version(_ endsWith "-SNAPSHOT"),
    bintrayOrganization := Some("maciej"),
    pomIncludeRepository := { _ => false },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    //noinspection ScalaUnnecessaryParentheses
    pomExtra := (
      <scm>
        <url>git@github.com:maciej/etcd-client.git</url>
        <connection>scm:git:git@github.com:maciej/etcd-client.git</connection>
      </scm>
        <developers>
          <developer>
            <id>maciej</id>
            <name>Maciej Bilas</name>
            <url>https://github.com/maciej</url>
          </developer>
          <developer>
            <id>rkrzewski</id>
            <name>Rafal Krzewski</name>
            <url>https://github.com/rkrzewski</url>
          </developer>
        </developers>
      ),
    releasePublishArtifactsAction := PgpKeys.publishSigned.value
  )
}
