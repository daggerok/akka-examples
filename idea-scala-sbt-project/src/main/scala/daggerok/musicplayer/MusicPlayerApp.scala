package daggerok.musicplayer

import akka.actor.{Actor, ActorSystem, Props}
import daggerok.musicplayer.MusicController.{PlayMsg, StopMsg}
import daggerok.musicplayer.MusicPlayer.{StartMusicMsg, StopMusicMsg}

object MusicController {
  sealed trait MusicControllerMessage
  case object PlayMsg extends MusicControllerMessage
  case object StopMsg extends MusicControllerMessage

  def props = Props[MusicControllerActor]
}

class MusicControllerActor extends Actor {
  override def receive: Receive = {
    case PlayMsg => println("playing music...")
    case StopMsg => println("music is stopped")
  }
}

object MusicPlayer {
  sealed trait MusicPlayerMessage
  case object StopMusicMsg extends MusicPlayerMessage
  case object StartMusicMsg extends MusicPlayerMessage
}

class MusicPlayerActor extends Actor {
  override def receive: Receive = {
    case StopMusicMsg => println("I don't wanna stop!")
    case StartMusicMsg =>
      val ctrlActor = context.actorOf(MusicController.props, "ctrl-actor")
      ctrlActor ! PlayMsg
    case _ => println("received unknown message.")
  }
}

object MusicPlayerApp {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("music-system")
    val actor = system.actorOf(Props[MusicPlayerActor], "mp-actor")

    actor ! StartMusicMsg
    actor ! StopMusicMsg
    Thread.sleep(1000)
    system.terminate()
  }
}
