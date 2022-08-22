package dev.sample.cncfziopravegademo

import zio._
import zio.pravega._

import io.pravega.client.stream.impl.UTF8StringSerializer

object StreamReadExample extends ZIOAppDefault {

  val stringReaderSettings =
    ReaderSettingsBuilder()
      .withSerializer(new UTF8StringSerializer)

  private val program = for {
    _ <- PravegaAdmin.createReaderGroup(
      "cncf-scope",
      "cncf-group",
      "utf8-string"
    )
    stream <- PravegaStream.stream(
      "cncf-group",
      stringReaderSettings
    )
    count <- stream
      .tap(m => ZIO.debug(m.toString()))
      .take(10)
      .runCount
    _ <- Console.printLine(s"Read $count elements.")

  } yield ()

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    program.provide(
      Scope.default,
      PravegaAdmin.live(PravegaClientConfig.default),
      PravegaStream.fromScope(
        "cncf-scope",
        PravegaClientConfig.default
      )
    )

}
