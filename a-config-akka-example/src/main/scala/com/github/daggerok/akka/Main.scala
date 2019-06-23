package com.github.daggerok.akka

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object Main extends App {
  private val regularConfig = ConfigFactory.load()
  private val system = ActorSystem("system", regularConfig)
  system.log.info("1) this system is uses regular application.conf config file:")
  system.log.info("1) {}", system.toString)
  system.log.info("1) log level: {}", regularConfig.getString("akka.loglevel"))
  system.terminate()

  private val inlineConfiguration =
    """
      |akka {
      |  loglevel = "ERROR"
      |}
      |magicNumber = 123
      |greeting {
      |  message = "Hej!"
      |}
    """.stripMargin
  private val config = ConfigFactory.load(inlineConfiguration)
  private val inlineSystem = ActorSystem("inline-config-system", regularConfig)
  inlineSystem.log.error("2) this system is uses inline config with error level:")
  inlineSystem.log.error("2) {}", inlineSystem.toString)
  inlineSystem.log.error("2) log level: {}", regularConfig.getString("akka.loglevel"))
  inlineSystem.log.error("2) magic number: {}", regularConfig.getInt("magicNumber"))
  inlineSystem.terminate()

  private val customNamespaceConfig = regularConfig.getConfig("customNamespace")
  private val customNamespaceSystem = ActorSystem("system-with-custom-namespace", customNamespaceConfig)
  customNamespaceSystem.log.warning("3) this system is uses custom namespace:")
  customNamespaceSystem.log.warning("3) {}", customNamespaceSystem.toString)
  customNamespaceSystem.log.warning("3) log level: {}", customNamespaceConfig.getString("akka.loglevel"))
  customNamespaceSystem.terminate()

  private val myOtherFileConfig = ConfigFactory.load("config/my-other-application.conf")
  private val myOtherFileSystem = ActorSystem("my-other-config-file-system", myOtherFileConfig)
  myOtherFileSystem.log.info("4) this is my expected info logs for system with external file config:")
  myOtherFileSystem.log.info("4) {}", myOtherFileSystem.toString)
  myOtherFileSystem.log.info("4) log level: {}", myOtherFileConfig.getString("akka.loglevel"))
  myOtherFileSystem.terminate()
}
