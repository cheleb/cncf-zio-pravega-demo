package dev.sample.ziodemo

import zio._

case class User(id: Int, name: String, addressId: Int)


case class UserRepo(dbname: String) extends AutoCloseable:
  def findById(id: Int): RIO[UserRepo, User] =
    ZIO.succeed(User(id, s"Derek", 1))
  override def close(): Unit = println("Closing")

object UserRepo:

  val live = ZLayer {
    ZIO.succeed(new UserRepo("test")).withFinalizerAuto
  }

  def findById(id: Int): RIO[UserRepo, User] =
    ZIO.serviceWithZIO(_.findById(id))

object ZLayerDemo extends ZIOAppDefault:

  val program = for {

    user <- UserRepo.findById(1)
    address <- AddressRepo.findById(user.addressId)
    _ <- PostalService.sendMail(user.id)
    _ <- Console.printLine(s"Hello ${user.name}")

  } yield ()




  
  override def run =
    for {
      _ <- Console.printLine("---------")
      _ <- program.provideSome(
        Scope.default,
        UserRepo.live,
        AddressRepo.live,
        PostalService.live,
      )
      _ <- Console.printLine("+++++++++")
    } yield ()

case class PostalService(userRepo: UserRepo, addressRepo: AddressRepo) {
  def sendMail(userId: Int) = Console.printLine(s"Mail sent to $userId")
}

object PostalService:

  val live = ZLayer.fromFunction((userRepo, addressRepo) =>
    PostalService(userRepo, addressRepo)
  )

  def sendMail(userId: Int) =
    ZIO.serviceWithZIO[PostalService](_.sendMail(userId))

case class Address(id: Int, country: String)

trait AddressRepo:
  def findById(id: Int): RIO[AddressRepo, Address]

case class AddressRepoImpl(dbname: String) extends AddressRepo:
  override def findById(id: Int): RIO[AddressRepo, Address] =
    ZIO.succeed(Address(id, s"Texas"))

object AddressRepo:

  val live = ZLayer {
    ZIO.succeed(new AddressRepoImpl("test"))
  }

  def findById(id: Int): RIO[AddressRepo, Address] =
    ZIO.serviceWithZIO(_.findById(id))
