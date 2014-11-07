package models

import java.net.URLDecoder

import com.hp.hpl.jena.query.QueryExecutionFactory
import org.joda.time.LocalDate
import utils.semantic._

import scala.concurrent.{ Promise, Future }

case class Student(
  gmId: String,
  firstname: String, lastname: String,
  registrationNumber: String,
  email: String,
  phone: String, degree: String)

object Students {

  import utils.Global._
  import utils.semantic.Vocabulary._

  import scala.concurrent.ExecutionContext.Implicits.global

  def create(student: Student): Future[Individual] = {
    val resource = ResourceUtils.createResource(lwmNamespace)
    val statements = List(
      Statement(resource, RDF.typ, LWM.Student),
      Statement(resource, RDF.typ, OWL.NamedIndividual),
      Statement(resource, LWM.hasGmId, StringLiteral(student.gmId.toLowerCase)),
      Statement(resource, FOAF.firstName, StringLiteral(student.firstname)),
      Statement(resource, FOAF.lastName, StringLiteral(student.lastname)),
      Statement(resource, RDFS.label, StringLiteral(s"${student.firstname} ${student.lastname}")),
      Statement(resource, NCO.phoneNumber, StringLiteral(student.phone)),
      Statement(resource, FOAF.mbox, StringLiteral(student.email)),
      Statement(resource, LWM.hasEnrollment, Resource(student.degree)),
      Statement(resource, LWM.hasRegistrationId, StringLiteral(student.registrationNumber))
    )

    sparqlExecutionContext.executeUpdate(SPARQLBuilder.insertStatements(statements: _*)).map { r ⇒
      Individual(resource)
    }
  }

  def delete(student: Student): Future[Student] = {
    val maybeStudent = SPARQLBuilder.listIndividualsWithClassAndProperty(LWM.Student, Vocabulary.LWM.hasGmId, StringLiteral(student.gmId))
    val resultFuture = sparqlExecutionContext.executeQuery(maybeStudent)
    val p = Promise[Student]()
    resultFuture.map { result ⇒
      val resources = SPARQLTools.statementsFromString(result).map(student ⇒ student.s)
      resources.map { resource ⇒
        sparqlExecutionContext.executeUpdate(SPARQLBuilder.removeIndividual(resource)).map { _ ⇒ p.success(student) }
      }
    }
    p.future
  }

  def delete(resource: Resource): Future[Resource] = {
    val p = Promise[Resource]()
    val individual = Individual(resource)
    if (individual.props(RDF.typ).contains(LWM.Student)) {
      sparqlExecutionContext.executeUpdate(SPARQLBuilder.removeIndividual(resource)).map { b ⇒ p.success(resource) }
    } else {
      p.failure(new IllegalArgumentException("Resource is not a Student"))
    }
    p.future
  }

  def all(): Future[List[Individual]] = {
    val query =
      s"""
         |select ?s (${RDF.typ} as ?p) (${LWM.Student} as ?o) where {
         | ?s ${RDF.typ} ${LWM.Student} .
         | optional {?s ${FOAF.lastName} ?lastname}
         |}order by asc(?lastname)
       """.stripMargin
    sparqlExecutionContext.executeQuery(query).map { stringResult ⇒
      SPARQLTools.statementsFromString(stringResult).map(student ⇒ Individual(student.s)).toList
    }
  }

  def get(gmId: String): Future[Resource] = {
    val p = Promise[Resource]()
    sparqlExecutionContext.executeQuery(SPARQLBuilder.listIndividualsWithClassAndProperty(LWM.Student, LWM.hasGmId, StringLiteral(gmId))).map { result ⇒
      val resource = SPARQLTools.statementsFromString(result).map(student ⇒ student.s)
      if (resource.nonEmpty) {
        p.success(resource.head)
      } else {
        p.failure(new NoSuchElementException(s"There is no student with ID $gmId"))
      }
    }
    p.future
  }

  private def search(query: String): Future[List[(String, String, String)]] = {
    val sparqlQuery =
      s"""
        |SELECT ?s (${LWM.hasGmId} as ?p) ?o where {?s ${LWM.hasGmId} ?o
        |FILTER regex(?o, "^$query")
        |}
      """.stripMargin

    val r = for {
      result ← sparqlExecutionContext.executeQuery(sparqlQuery)
    } yield {
      val statements = SPARQLTools.statementsFromString(result)
      statements.map { statement ⇒
        val individual = Individual(statement.s)
        val name = individual.props.getOrElse(RDFS.label, List(StringLiteral(""))).head.value
        (statement.o.value, name, statement.s.value)
      }
    }
    r.map(_.toList)
  }

  def search(query: String, maxCount: Int): Future[List[(String, String, String)]] = if (maxCount > 0) search(query).map(_.sortBy(_._1).take(maxCount)) else search(query).map(_.sortBy(_._1))

  def exists(uid: String): Future[Boolean] = sparqlExecutionContext.executeBooleanQuery(s"ASK {?s ${Vocabulary.LWM.hasGmId} ${StringLiteral(uid.toLowerCase).toQueryString}}")

  def isStudent(resource: Resource): Future[Boolean] = sparqlExecutionContext.executeBooleanQuery(s"ASK {$resource ${Vocabulary.RDF.typ} ${LWM.Student}}")

  def labworksForStudent(student: Resource) = {
    val query1 =
      s"""
         |select * where {
         | $student ${LWM.memberOf} ?group .
         | ?group ${LWM.hasLabWork} ?labwork.
         | ?labwork ${RDFS.label} ?labworkName .
         |}order by desc(?labworkName)
       """.stripMargin
    val results = QueryExecutionFactory.sparqlService(queryHost, query1).execSelect()
    var mapping = List.empty[(Resource, String)]
    while (results.hasNext) {
      val solution = results.nextSolution()
      val labworkResource = solution.getResource("labwork")
      val name = if (solution.getLiteral("labworkName") == null) "" else URLDecoder.decode(solution.getLiteral("labworkName").getString, "UTF-8")
      if (labworkResource != null) {
        mapping = (Resource(labworkResource.getURI), name) :: mapping
      }
    }
    mapping
  }

  def dateCountMissed(student: Resource): Int = {
    import utils.Implicits._
    val q = s"""
        prefix lwm: <http://lwm.gm.fh-koeln.de/>

        select (count(?attended) as ?count) where {
          $student lwm:hasScheduleAssociation ?association .
          ?association lwm:hasAssignmentDate ?date .
          optional{?association lwm:hasAttended ?attended} .
          optional{
              ?association lwm:hasAlternateScheduleAssociation ?alternate .
              ?alternate lwm:hasAssignmentDate ?alternateDate .
            } .
          filter(?date < "${LocalDate.now().plusDays(1).toString("yyyy-MM-dd")}")
          filter(?attended = "false")
          filter(?alternateDate < "${LocalDate.now().toString("yyyy-MM-dd")}")
        }
     """.stripMargin

    q.execSelect().headOption.map { solution ⇒
      solution.data.get("count").map(_.asLiteral().getInt)
    }.flatten.getOrElse(0)
  }
  def dateCountNotPassed(student: Resource): Int = {
    import utils.Implicits._
    s"""
        prefix lwm: <http://lwm.gm.fh-koeln.de/>

        select (count(?passed) as ?count) where {
          $student lwm:hasScheduleAssociation ?association .
          ?association lwm:hasAssignmentDate ?date .
          optional{?association lwm:hasPassed ?passed} .
          filter(?date < "${LocalDate.now().toString("yyyy-MM-dd")}")
          filter(?passed = "false")
        }
     """.stripMargin.execSelect().headOption.map { solution ⇒
      solution.data.get("count").map(_.asLiteral().getInt)
    }.flatten.getOrElse(0)
  }
}

object StudentForms {
  import play.api.data.Forms._
  import play.api.data._
}

