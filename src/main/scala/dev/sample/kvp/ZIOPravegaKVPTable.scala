package dev.sample.kvp

import zio._
import zio.pravega._
import io.pravega.client.stream.Serializer
import model.Person
import java.nio.ByteBuffer
import zio.stream.ZStream
import io.pravega.client.stream.impl.UTF8StringSerializer
import zio.stream.ZSink

object ZIOPravegaKVPTable extends ZIOAppDefault {

  val clientConfig = PravegaClientConfig.default

  def program = for {
    _ <- writeToPersonStream
    _ <- concurrentWriteToTableAges
    _ <- readAges
  } yield ()

  def writeToPersonStream = for {
    sink <- PravegaStream.sink("person-stream", personStremWritterSettings)
    res <- testStream(0, 100) >>> sink
  } yield res

 


   def writeToTableAges = for {

    tableSink <- PravegaTable.sink("ages", tableWriterSettings, _ + _)

    stream1 <- PravegaStream.stream("ages-counter", personReaderSettings)

    _ <- stream1.take(100).map(p => (p.age, 1)).run(tableSink)
  } yield ()


  def streamToTable(
      tableSink: ZSink[Any, Throwable, (Int, Int), Nothing, Unit]
  ) = for {
    stream1 <- PravegaStream.stream("ages-counter", personReaderSettings)
    _ <- stream1.take(50).map(p => (p.age, 1)).run(tableSink)
  } yield ()

  def concurrentWriteToTableAges = for {

    tableSink <- PravegaTable.sink("ages", tableWriterSettings, _ + _)

    fiber <- streamToTable(tableSink).fork
    _ <- streamToTable(tableSink)
    _ <- fiber.join
  } yield ()

 
  def readAges = for {
    source <- PravegaTable.source("ages", tableReaderSettings)
    ages <- source.take(10) >>> ZSink.collectAll
    _ <- ZIO.debug(
      ages
        .map(e =>
          s"${intSerializer.deserialize(e.tableKey.getPrimaryKey())}: ${e.value}"
        )
        .mkString("\n")
    )
  } yield ()

  override def run = program.provide(
    Scope.default,
    PravegaStream.fromScope("cncf-scope", clientConfig),
    PravegaTable
      .fromScope("cncf-scope", clientConfig)
  )

  val intSerializer = new Serializer[Int] {
    override def serialize(value: Int): ByteBuffer = {
      val buff = ByteBuffer.allocate(4).putInt(value)
      buff.position(0)
      buff
    }

    override def deserialize(serializedValue: ByteBuffer): Int =
      serializedValue.getInt
  }

  private val personSerializer = new Serializer[Person] {

    override def serialize(person: Person): ByteBuffer =
      ByteBuffer.wrap(person.toByteArray)

    override def deserialize(buffer: ByteBuffer): Person =
      Person.parseFrom(buffer.array())

  }

  val tableWriterSettings = TableWriterSettingsBuilder(
    intSerializer,
    intSerializer
  )
    .build()

  val tableReaderSettings = TableReaderSettingsBuilder(
    intSerializer,
    intSerializer
  )
    .build()

  val personStremWritterSettings =
    WriterSettingsBuilder()
      .withSerializer(personSerializer)

  val personReaderSettings =
    ReaderSettingsBuilder()
      .withSerializer(personSerializer)

  private def testStream(a: Int, b: Int): ZStream[Any, Nothing, Person] =
    ZStream
      .fromIterable(a until b)
      .map(i => Person(key = f"$i%04d", name = s"name $i", age = i % 10))

}
