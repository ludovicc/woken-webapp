package models

case class Variable(code: String)

case class AlgorithmParam(code: String, value: String)

case class Algorithm(code: String, name: String, parameters: Seq[AlgorithmParam], validation: Option[Boolean] = None)

case class MiningQuery(id: Long, algorithm: Algorithm, filters: String, variables: Seq[Variable], covariables: Seq[Variable], grouping: Seq[Variable], datasets: Seq[Variable])

case class Mining(jobId: String, node: String, function: String, shape: String, timestamp: Long, data: String)
