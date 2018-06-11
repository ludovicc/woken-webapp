import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import sangria.ast.Document
import sangria.execution._
import sangria.parser.QueryParser
import sangria.marshalling.sprayJson._
import service.WokenService
import spray.json.{JsObject, JsString, JsValue}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object GraphQLServer {

  val wokenService = new WokenService

  def endpoint(requestJSON: JsValue)(implicit ec: ExecutionContext): Route = {
    val JsObject(fields) = requestJSON
    val JsString(query) = fields("query")

    QueryParser.parse(query) match {
      case Success(queryAst) =>
        val operation = fields.get("operationName") collect {
          case JsString(op) => op
        }

        val variables = fields.get("variables") match {
          case Some(obj: JsObject) => obj
          case _ => JsObject.empty
        }

        complete(executeGraphQLQuery(queryAst, operation, variables))

      case Failure(error) =>
        complete(StatusCodes.BadRequest, JsObject("error" -> JsString(error.getMessage)))
    }
  }

  private def executeGraphQLQuery(query: Document, operation: Option[String], vars: JsObject)(implicit ec: ExecutionContext) = {
    Executor.execute(
      GraphQLSchema.SchemaDefinition,
      query,
      WokenContext(wokenService),
      variables = vars,
      operationName = operation
    ).map(StatusCodes.OK -> _).recover {
      case error: QueryAnalysisError => StatusCodes.BadRequest -> error.resolveError
      case error: ErrorWithResolver => StatusCodes.InternalServerError -> error.resolveError
    }
  }

}
