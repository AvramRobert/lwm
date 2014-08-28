package models

import play.api.data.Form
import play.api.data.Forms._
import utils.semantic._

import scala.concurrent.Future



case class LabWork(id: String, name: String, groupCount: Int, assignmentCount: Int, courseId: String, degreeId: String, semester: String)

case class LabWorkApplication(courseID: String, gmID: String)

/**
 * An assignment group.
 * @param groupID id of this group
 * @param courseID id of this associated course
 * @param students gmIds of the students
 */
case class LabWorkGroup(groupID: String, courseID: String, students: List[String])
// TODO course id should be courseResourceURI


object LabWorkForms{
  val labWorkApplicationForm = Form(
    mapping(
      "courseID" -> nonEmptyText,
      "gmID" -> nonEmptyText
    )(LabWorkApplication.apply)(LabWorkApplication.unapply)
  )

  val labworkForm = Form(
    mapping(
      "id" -> nonEmptyText,
      "name" -> nonEmptyText,
      "groupCount" -> number(min=1),
      "assignmentCount" -> number(min=1),
      "courseId" -> nonEmptyText,
      "degreeId" -> nonEmptyText,
      "semester" -> nonEmptyText
    )(LabWork.apply)(LabWork.unapply)
  )
}

/**
 * Praktika
 */
object LabWorks {
  import utils.Global._
  import utils.semantic.Vocabulary._

  import scala.concurrent.ExecutionContext.Implicits.global

  def create(labWork: LabWork): Future[Individual] = Future {
    val resource = ResourceUtils.createResource(lwmNamespace)
    val statements = List(
      Statement(resource, RDF.typ, LWM.LabWork),
      Statement(resource, RDF.typ, OWL.NamedIndividual),
      Statement(resource, LWM.hasId, Literal(labWork.id)),
      Statement(resource, LWM.hasName, Literal(labWork.name)),
      Statement(resource, LWM.hasAssignmentCount, Literal(labWork.assignmentCount.toString)),
      Statement(resource, LWM.hasCourse, Resource(labWork.courseId)),
      Statement(resource, LWM.hasDegree, Resource(labWork.degreeId)),
      Statement(resource, LWM.hasSemester, Literal(labWork.semester))
    )

    for(i <- 0 until labWork.groupCount) {
      val group = ResourceUtils.createResource(lwmNamespace)
      val groupStatements = List(
        Statement(group, RDF.typ, LWM.Group),
        Statement(group, RDF.typ, OWL.NamedIndividual),
        Statement(resource, LWM.hasGroup, group)
      )
      sparqlExecutionContext.executeUpdate(SPARQLBuilder.insertStatements(lwmGraph,  groupStatements: _*))
    }

    sparqlExecutionContext.executeUpdate(SPARQLBuilder.insertStatements(lwmGraph,  statements: _*))

    Individual(resource)
  }

  def delete(labwork: LabWork) = {
    val maybeLabwork = SPARQLBuilder.listIndividualsWithProperty(Vocabulary.LWM.hasId, Literal(labwork.id))
    val degreeResource = SPARQLTools.statementsFromString(sparqlExecutionContext.executeQuery(maybeLabwork)).map(labwork => labwork.s)
    degreeResource.map(res => sparqlExecutionContext.executeUpdate(SPARQLBuilder.removeIndividual(res, lwmGraph)))
  }
  def delete(id: String) = {
    val maybeCourse = SPARQLBuilder.listIndividualsWithProperty(Vocabulary.LWM.hasId, Literal(id))
    val courseResource = SPARQLTools.statementsFromString(sparqlExecutionContext.executeQuery(maybeCourse)).map(course => course.s)
    courseResource.map(res => sparqlExecutionContext.executeUpdate(SPARQLBuilder.removeIndividual(res, lwmGraph)))
  }
  def all(): Future[Seq[Individual]] = Future{
    SPARQLTools.statementsFromString(sparqlExecutionContext.executeQuery(SPARQLBuilder.listIndividualsWithClass(LWM.LabWork))).map(labwork => Individual(labwork.s))
  }
}