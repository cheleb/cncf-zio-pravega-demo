package dev.sample.kvp

import zio._
import zio.pravega._
import io.pravega.client.stream.Serializer
import model.Person
import java.nio.ByteBuffer
import io.pravega.client.tables.KeyValueTableConfiguration
import io.pravega.client.stream.StreamConfiguration
import io.pravega.client.stream.ScalingPolicy

object ZIOPravegaKVPResources extends ZIOAppDefault {

  private val streamConfiguration = StreamConfiguration.builder
    .scalingPolicy(ScalingPolicy.fixed(8))
    .build

  private val tableConfig = KeyValueTableConfiguration
    .builder()
    .partitionCount(2)
    .primaryKeyLength(4)
    .build()

  val program = for {

    personStreamCreated <- PravegaAdmin.createStream(
      "cncf-scope",
      "person-stream",
      streamConfiguration
    )
    _ <- ZIO.debug(
      s"Stream \"person-stream\" newly created: $personStreamCreated"
    )

    created <- PravegaAdmin.createTable(
      "ages",
      tableConfig,
      "cncf-scope"
    )
    _ <- ZIO.debug(s"Table ages created: $created")

    agesCounter <- PravegaAdmin.createReaderGroup(
      "cncf-scope",
      "ages-counter",
      "person-stream"
    )

    _ <- ZIO.debug(s"ages counter reader group created: $created")
  } yield ()
  override def run =
    program.provide(
      Scope.default,
      PravegaAdmin.live(PravegaClientConfig.default)
    )

}
