import models._
import sangria.schema._
import sangria.marshalling.sprayJson._
import spray.json.DefaultJsonProtocol._

object GraphQLSchema {

  implicit val algorithmParamJsonFormat = jsonFormat2(models.AlgorithmParam)
  implicit val algorithmJsonFormat = jsonFormat4(models.Algorithm)
  implicit val variableJsonFormat = jsonFormat1(models.Variable)
  implicit val miningQueryJsonFormat = jsonFormat7(models.MiningQuery)

  val AlgorithmParam: InputObjectType[AlgorithmParam] = InputObjectType[AlgorithmParam](
    "AlgorithmParam",
    "A parameter definition belonging to an algorithm",
    List(
      InputField("code", StringType),
      InputField("value", StringType)
    )
  )

  val Algorithm: InputObjectType[Algorithm] = InputObjectType[Algorithm](
    "Algorithm",
    "Algorithm definition",
    List(
      InputField("code", StringType),
      InputField("name", StringType),
      InputField("parameters", ListInputType(AlgorithmParam)),
      InputField("validation", OptionInputType(BooleanType))
    )
  )

  val Variable: InputObjectType[Variable] = InputObjectType[Variable](
    "Variable",
    "A data variable definition",
    List(
      InputField("code", StringType)
    )
  )

  val MiningQuery: InputObjectType[MiningQuery] = InputObjectType[MiningQuery](
    "MiningQuery",
    "A query for data mining.",
    List(
      InputField("id", LongType),
      InputField("algorithm", Algorithm),
      InputField("filters", StringType),
      InputField("variables", ListInputType(Variable)),
      InputField("covariables", ListInputType(Variable)),
      InputField("grouping", ListInputType(Variable)),
      InputField("datasets", ListInputType(Variable))
    ))

  val Mining = ObjectType(
    "Mining",
    fields[Unit, Mining](
      Field("jobId", StringType, resolve = _.value.jobId),
      Field("node", StringType, resolve = _.value.node),
      Field("function", StringType, resolve = _.value.function),
      Field("shape", StringType, resolve = _.value.shape),
      Field("timestamp", LongType, resolve = _.value.timestamp),
      Field("data", StringType, resolve = _.value.data)
    )
  )

  val QueryArg = Argument("query", MiningQuery)

  val QueryType = ObjectType(
    "Query",
    fields[WokenContext, Unit](
      Field("mining", Mining, arguments = List(QueryArg),
        resolve = c => c.ctx.wokenService.sendMiningQuery(c.arg(QueryArg)))
    )
  )

  val SchemaDefinition = Schema(QueryType)
}
