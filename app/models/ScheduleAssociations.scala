package models

import java.util.UUID

import org.joda.time.{ LocalTime, LocalDate }
import play.api.data.Form
import play.api.data.Forms._
import utils.Global._
import utils.semantic.Vocabulary.{ LWM, OWL, RDF, RDFS }
import utils.semantic._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Future, Promise }

case class ScheduleAssociation(group: Resource, assignmentAssoc: Resource, assignmentDate: LocalDate, dueDate: LocalDate, assignmentDateTimetableEntry: Resource, dueDateTimetableEntry: Resource)

object ScheduleAssociations {
  def create(assignment: ScheduleAssociation): Future[Individual] = {
    val id = UUID.randomUUID()
    val assocResource = ResourceUtils.createResource(lwmNamespace, id)

    val statements = List(
      Statement(assocResource, RDF.typ, LWM.ScheduleAssociation),
      Statement(assocResource, RDF.typ, OWL.NamedIndividual),
      Statement(assocResource, LWM.hasAssignmentDate, DateLiteral(assignment.assignmentDate)),
      Statement(assocResource, LWM.hasDueDate, DateLiteral(assignment.dueDate)),
      Statement(assocResource, LWM.hasGroup, assignment.group),
      Statement(assignment.group, LWM.hasScheduleAssociation, assocResource),
      Statement(assocResource, LWM.hasDueDateTimetableEntry, assignment.dueDateTimetableEntry),
      Statement(assocResource, LWM.hasAssignmentDateTimetableEntry, assignment.assignmentDateTimetableEntry),
      Statement(assocResource, LWM.hasAssignmentAssociation, assignment.assignmentAssoc)
    )

    sparqlExecutionContext.executeUpdate(SPARQLBuilder.insertStatements(statements: _*)).map(b ⇒ Individual(assocResource))
  }

  def delete(resource: Resource): Future[Resource] = {
    val p = Promise[Resource]()
    val individual = Individual(resource)
    if (individual.props(RDF.typ).contains(LWM.ScheduleAssociation)) {
      sparqlExecutionContext.executeUpdate(SPARQLBuilder.removeIndividual(resource)).map { b ⇒ p.success(resource) }
    } else {
      p.failure(new IllegalArgumentException("Resource is not an ScheduleAssociation"))
    }
    p.future
  }

  def all(): Future[List[Individual]] = {
    sparqlExecutionContext.executeQuery(SPARQLBuilder.listIndividualsWithClass(LWM.ScheduleAssociation)).map { stringResult ⇒
      SPARQLTools.statementsFromString(stringResult).map(course ⇒ Individual(course.s)).toList
    }
  }

  def dates(group: Resource, association: Resource): (LocalDate, LocalDate) = {
    val query1 =
      s"""
        |SELECT ?s (${LWM.hasAssignmentDate} as ?p) ?o where {
        | ${group.toQueryString} ${LWM.hasScheduleAssociation} ?s .
        | ?s ${LWM.hasAssignmentAssociation} ${association.toQueryString} .
        | ?s ${LWM.hasAssignmentDate} ?o .
        |}
      """.stripMargin

    val query2 =
      s"""
        |SELECT ?s (${LWM.hasDueDate} as ?p) ?o where {
        | ${group.toQueryString} ${LWM.hasScheduleAssociation} ?s .
        | ?s ${LWM.hasAssignmentAssociation} ${association.toQueryString} .
        | ?s ${LWM.hasDueDate} ?o .
        |}
      """.stripMargin

    (LocalDate.parse(SPARQLTools.statementsFromString(sparqlExecutionContext.executeQueryBlocking(query1)).head.o.value), LocalDate.parse(SPARQLTools.statementsFromString(sparqlExecutionContext.executeQueryBlocking(query2)).head.o.value))
  }

  def times(group: Resource, association: Resource): (Time, Time) = {
    val query1 =
      s"""
        |SELECT ?s (${LWM.hasStartTime} as ?p) ?o where {
        | ${group.toQueryString} ${LWM.hasScheduleAssociation} ?sca .
        | ?sca ${LWM.hasAssignmentAssociation} ${association.toQueryString} .
        | ?sca ${LWM.hasAssignmentDateTimetableEntry} ?s .
        | ?s ${LWM.hasStartTime} ?o .
        |}
      """.stripMargin

    val query2 =
      s"""
        |SELECT ?s (${LWM.hasStartTime} as ?p) ?o where {
        | ${group.toQueryString} ${LWM.hasScheduleAssociation} ?sca .
        | ?sca ${LWM.hasAssignmentAssociation} ${association.toQueryString} .
        | ?sca ${LWM.hasDueDateTimetableEntry} ?s .
        | ?s ${LWM.hasStartTime} ?o .
        |}
      """.stripMargin

    val time1 = SPARQLTools.statementsFromString(sparqlExecutionContext.executeQueryBlocking(query1)).head.o.asLiteral().get.decodedString.split(":")
    val time2 = SPARQLTools.statementsFromString(sparqlExecutionContext.executeQueryBlocking(query2)).head.o.asLiteral().get.decodedString.split(":")
    val h1 = time1(0).toInt
    val m1 = time1(1).toInt

    val h2 = time2(0).toInt
    val m2 = time2(1).toInt
    (Time(h1, m1), Time(h2, m2))
  }
}
