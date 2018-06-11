import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.settings.ServerSettings
import com.typesafe.config.ConfigFactory
import sangria.renderer.SchemaRenderer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global


object HttpBackendServer extends HttpApp {

  def routes =
    path("graphql") {
      post {
        entity(as[JsValue]) { requestJson =>
          GraphQLServer.endpoint(requestJson)
        }
      }
    } ~
    path("graphiql") {
      get {
        getFromResource("graphiql.html")
      }
    } ~
    get {
      complete {
        SchemaRenderer.renderSchema(GraphQLSchema.SchemaDefinition)
      }
    }
}

object Server extends App {
  HttpBackendServer.startServer("0.0.0.0", 9090, ServerSettings(ConfigFactory.load))
}
