package dev.sample.cncfziopravegademo

import zio._
import zio.pravega._

import io.pravega.client.stream.StreamConfiguration
import io.pravega.client.stream.ScalingPolicy

object CreateResourcesExample extends ZIOAppDefault {

  private val streamConfiguration = StreamConfiguration.builder
    .scalingPolicy(ScalingPolicy.fixed(8))
    .build

  private val program = for {

    scopeCreated <- PravegaAdmin.createScope("cncf-scope")
    _ <- ZIO.debug(s"Scope newly created: $scopeCreated")

    stringStreamCreated <- PravegaAdmin.createStream(
      "cncf-scope",
      "utf8-string",
      streamConfiguration
    )
    _ <- ZIO.debug(
      s"Stream \"utf8-string\" newly created: $stringStreamCreated"
    )

  } yield ()

  override def run: ZIO[Any, Throwable, Unit] =
    program
      .provide(
        Scope.default,
        PravegaAdmin.live(PravegaClientConfig.default)
      )

}
