package com.github.daggerok.akka

import akka.actor._

object BankAccount {
  sealed trait Operation
  case class Deposit(amount: BigDecimal) extends Operation
  case class Withdraw(amount: BigDecimal) extends Operation
  case object PrintReport extends Operation

  sealed trait Result
  case class TransactionSuccess(result: String) extends Result
  case class TransactionFailure(reason: String) extends Result
}

class BankAccount extends Actor {
  import BankAccount._
  private val log = context.system.log
  private var funds: BigDecimal = 0.00
  override def receive: Receive = {
    case PrintReport => sender() ! s"${self.path.name} current balance: $funds"
    case Deposit(amount) =>
      if (amount < 0.00) sender() ! TransactionFailure("Invalid amount")
      else {
        funds += amount
        sender() ! TransactionSuccess(s"Successfully deposited $amount")
      }
    case Withdraw(amount) =>
      if (amount <= 0.00) sender() ! TransactionFailure("Invalid amount")
      else if (funds < amount) sender() ! TransactionFailure(s"Not enough money! Withdraw request for $amount cannot be completed...")
      else {
        funds -= amount
        sender ! TransactionSuccess(s"Successfully withdrew $amount")
      }
  }
}

object Manager {
  case class Simulate(account: ActorRef)
}

class Manager extends Actor {
  import Manager._
  import BankAccount._
  override def receive: Receive = {
    case Simulate(account) =>
      account ! Deposit(10.00)
      account ! Withdraw(50.00)
      account ! Withdraw(5.55)
      account ! PrintReport
    case message => context.system.log.info("{}", message)
  }
}

object Main extends App {
  val system = ActorSystem("system")
  val bankAccount = system.actorOf(Props[BankAccount], "bankAccount")
  val manager = system.actorOf(Props[Manager], "manager")

  import Manager._
  manager ! Simulate(bankAccount)

  system.terminate()
}
