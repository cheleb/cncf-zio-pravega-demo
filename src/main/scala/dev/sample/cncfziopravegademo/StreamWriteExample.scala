package dev.sample.cncfziopravegademo

import zio._
import zio.pravega._
import zio.stream._

import io.pravega.client.stream.impl.UTF8StringSerializer

object StreamWriteExample extends ZIOAppDefault {

  val clientConfig = PravegaClientConfig.default

  val stringWriterSettings =
    WriterSettingsBuilder()
      .eventWriterConfigBuilder(_.enableLargeEvents(true))
      .withSerializer(new UTF8StringSerializer)

  private def testStream(a: Int, b: Int): ZStream[Any, Nothing, String] =
    ZStream
      .fromIterable(a to b)
      .map(i => s"Event n° $i")

  val program = for {
    sink <- PravegaStream.sink(
      "utf8-string",
      stringWriterSettings
    )
    _ <- testStream(0, 10)
      .tap(p => ZIO.debug(p.toString()))
      .run(sink)

  } yield ()

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    program.provide(
      Scope.default,
      PravegaStream.fromScope("cncf-scope", clientConfig)
    )

}
