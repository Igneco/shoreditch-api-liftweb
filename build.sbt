import scala.util.Try


name := "shoreditch-api-liftweb"

organization := "im.mange"

version := Try(sys.env("TRAVIS_BUILD_NUMBER")).map("0.2." + _).getOrElse("1.0-SNAPSHOT")

scalaVersion:= "2.11.7"

resolvers ++= Seq(
  "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"
)

resolvers     += Resolver.sonatypeRepo("releases")

libraryDependencies ++= Seq(
  "net.liftweb" %% "lift-webkit" % "[2.6.1,2.7.0]" % "provided",
  "im.mange" %% "shoreditch-api" % "[0.0.68,0.1.0]" % "provided",
  "org.scalatest" % "scalatest_2.11" % "[2.2.1,2.3.0]" % "test"
)

sonatypeSettings

publishTo <<= version { project_version ⇒
  val nexus = "https://oss.sonatype.org/"
  if (project_version.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

homepage := Some(url("https://github.com/alltonp/shoreditch-api-liftweb"))

licenses +=("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", System.getenv("SONATYPE_USER"), System.getenv("SONATYPE_PASSWORD"))

pomExtra :=
    <scm>
      <url>git@github.com:alltonp/shoreditch-api-liftweb.git</url>
      <connection>scm:git:git@github.com:alltonp/shoreditch-api-liftweb.git</connection>
    </scm>
    <developers>
      <developer>
        <id>alltonp</id>
      </developer>
    </developers>
