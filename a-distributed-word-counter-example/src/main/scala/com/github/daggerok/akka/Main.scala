package com.github.daggerok.akka

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object Master {
  case class InitializeCluster(size: Int)
  case class WorkCountTask(text: String, jobId: UUID = UUID.randomUUID())
  case class WordCounterResult(worker: String, jobId: UUID, source: String, total: Number, unique: Number) {
    override def toString: String = s"${getClass.getSimpleName}($source => $worker: $unique/$total)"
  }
}

class Master extends Actor {
  import Master._

  override def receive: Receive = {
    case InitializeCluster(size) =>
      val clusterRefs = for (i <- 1 to size)
        yield context.actorOf(Props[Worker], s"worker-$i")
      context.system.log.info("{}-node cluster initialized", size)
      context.become(initializedWith(clusterRefs, 0))
  }

  def initializedWith(clusterRefs: IndexedSeq[ActorRef], workerIndex: Int): Receive = {
    case text: String =>
      val workerRef: ActorRef = clusterRefs(workerIndex)
      workerRef ! WorkCountTask(text)
      val nextWorkerIndex = (workerIndex + 1) % clusterRefs.size
      context.become(initializedWith(clusterRefs, nextWorkerIndex))
    case result: WordCounterResult =>
      context.system.log.info(result.toString)
  }
}

class Worker extends Actor {
  import Master._
  override def receive: Receive = {
    case WorkCountTask(text, uuid) =>
      val words = text.trim.split("\\s+").toList
      val groupingMap = words.groupBy(k => k)
      val uniques = groupingMap.map/*[List[String]]*/(_._2)
      sender() ! WordCounterResult(self.path.name, uuid, text, words.size, uniques.size)
  }
}

object Main extends App {
  val system = ActorSystem("master-workers-system")
  val master = system.actorOf(Props[Master], "master")
  val batch = List(
    " ololo   trololo ho ho ho ho ho                  ",
    " ololo   trololo ho ho ho ho ho nono no          ",
    " ololo   trololo ho ho ho ho ho nono nono        ",
    " ololo   trololo ho ho ho ho ho no no no no      ",
    " o lo lo   tro lo lo ho ho ho ho ho no no no no  ",
    " 1 2 3 4 5 6 7 8 9 0 9 8 7 6 5 4 3 2 1           ",
    " 0 0 1 0 1 2 0 1 2 3 0 1 2 3 4 0 1 2 3 4 5       ",
    " 1 2 3 2 1                                       ",
  )
  import Master._
  master ! InitializeCluster(batch.size / 2)
  batch.foreach(text => master ! text)
  system.terminate()
}
