name := "PlayScalaCars"

version := "1.0"

lazy val `playscalacars` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( jdbc , cache , ws,
  "mysql" % "mysql-connector-java" % "5.1.36")

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  