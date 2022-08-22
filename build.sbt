// give the user a nice default project!
organization := "dev.sample"
scalaVersion := "3.1.3"
run / fork := true
name := "cncf-zio-pravega-demo"
scalafmtOnCompile := true
libraryDependencies ++= Seq(
  "dev.cheleb" %% "zio-pravega" % "0.1.1" exclude("org.apache.logging.log4j", "log4j-core"),
  "ch.qos.logback" % "logback-classic" % "1.2.11",
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,
  "io.envoyproxy.protoc-gen-validate" % "pgv-java-stub" % "0.6.7",
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.11" % "2.5.0-3" % "protobuf",
  "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.11" % "2.5.0-3",
  "com.thesamet.scalapb.common-protos" %% "pgv-proto-scalapb_0.11" % "0.6.3-0" % "protobuf",
  "com.thesamet.scalapb.common-protos" %% "pgv-proto-scalapb_0.11" % "0.6.3-0"
)

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
)
