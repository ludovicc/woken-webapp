package service

import akka.actor.{ActorPath, ActorPaths, ActorRef}
import akka.pattern.ask
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.util.Timeout
import models.{Mining, MiningQuery}

import collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class WokenService {

  import akka.actor.ActorSystem
  import com.typesafe.config.Config
  import com.typesafe.config.ConfigFactory

  implicit val timeout: Timeout = 5 seconds

  val config: Config = ConfigFactory.load("application.conf")
  val system: ActorSystem = ActorSystem.create("GraphQL-API", config)

  def initialContacts: Set[ActorPath] = {
    config.getStringList("akka.cluster.client.initial-contacts").asScala
      .map(path => ActorPaths.fromString(path))
      .toSet[ActorPath]
  }

  val wokenClient: ActorRef = system.actorOf(ClusterClient.props(
    ClusterClientSettings.create(system).withInitialContacts(initialContacts)),
    "client-" + getClass.getSimpleName)


  def sendMiningQuery(query: MiningQuery): Mining = {

    val sendMessage = ClusterClient.Send("/user/entrypoint", query, localAffinity = true)
    val queryResponse = wokenClient ? sendMessage

    Await.result(queryResponse, 10 seconds).asInstanceOf[Mining]
  }

}
