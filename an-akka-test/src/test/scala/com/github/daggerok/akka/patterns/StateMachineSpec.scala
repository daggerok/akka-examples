package com.github.daggerok.akka.patterns

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Stash}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

object StateMachine {
  type Product = String
  type Quantity = Int
  type Price = BigDecimal
  case class Initialize(inventory: Map[Product, Quantity], prices: Map[Product, Price])
  case class Status(inventory: Map[Product, Quantity], prices: Map[Product, Price])
  case object OutOfService
  case class Request(product: Product)
  class StateMachineActor extends Actor with ActorLogging {
    override def receive: Receive = pending(Map(), Map())
    def pending(inventory: Map[Product, Quantity], prices: Map[Product, Price]): Receive = {
      case Initialize(newInventory, newPrices) =>
        val fullInventory = concat(inventory, newInventory)
        val fullPrices = prices ++ newPrices
        log.info("initialized with: {}", info(fullInventory, fullPrices))
        context.become(ready(fullInventory, fullPrices))
      case _ => sender() ! Status(inventory, prices)
    }
    def ready(inventory: Map[Product, Quantity], prices: Map[Product, Price]): Receive = {
      case OutOfService =>
        log.warning("state machine is under maintenance...")
        context.become(pending(inventory, prices))
      case _ => sender() ! Status(inventory, prices)
    }
    def concat(map1: Map[Product, Quantity], map2: Map[Product, Quantity]) = {
      val allProducts = map1.keySet ++ map2.keySet
      val setOfTuples = allProducts.map(p => p -> (map1.getOrElse(p, 0) + map2.getOrElse(p, 0)))
      setOfTuples.groupBy(_._1).mapValues(s => s.map(_._2).sum)
    }
    def info(inventory: Map[Product, Quantity], prices: Map[Product, Price]) =
      inventory.map(i => i._1 + "|" + i._2 + "|" + prices(i._1)).mkString(",")
  }
}

class StateMachineSpec extends TestKit(ActorSystem("state-machine-system-under-test"))
  with ImplicitSender with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "a state machine actor" should {
    import StateMachine._

    "behave empty" in {
      val actorRef = system.actorOf(Props[StateMachineActor], s"state-${System.nanoTime()}")
      actorRef ! "whatever"
      val status = expectMsgType[Status]
      assert(status.inventory.isEmpty)
      assert(status.prices.isEmpty)
    }

    "behave as expected" in {
      val actorRef = system.actorOf(Props[StateMachineActor], s"state-${System.nanoTime()}")
      val initialize = Initialize(
        Map("kokoko" -> 1),
        Map("kokoko" -> 1.20)
      )

      actorRef ! initialize

      actorRef ! "whatever"
      val status = expectMsgType[Status]

      assert(status.inventory("kokoko") == 1)
      assert(status.prices("kokoko") == 1.20)
    }

    "have a correct map replace operation" in {
      val expected = Map[Product, Quantity]("ololo" -> 1, "trololo" -> 3, "hohoho" -> 5)
      val willBeReplacedInAddition = Map[Product, Quantity]("ololo" -> 2, "trololo" -> 3)
      val additionalReplacement = Map[Product, Quantity]("ololo" -> 1, "hohoho" -> 5)
      val actual = willBeReplacedInAddition ++ additionalReplacement
      assert(expected == actual)
    }

    "have a correct map concat operation" in {
      val expected = Map[Product, Quantity]("nonono" -> 0, "ololo" -> 3, "trololo" -> 3, "hohoho" -> 5)
      val curr = Map[Product, Quantity]("ololo" -> 2, "trololo" -> 3, "nonono" -> 0)
      val toBeAdded = Map[Product, Quantity]("ololo" -> 1, "hohoho" -> 5)
      val allProducts = curr.keySet ++ toBeAdded.keySet
      val setOfTuples = allProducts.map(p => p -> (curr.getOrElse(p, 0) + toBeAdded.getOrElse(p, 0)))
      val actual = setOfTuples.groupBy(_._1).mapValues(s => s.map(_._2).sum)
      assert(actual == expected)
    }
  }
}
