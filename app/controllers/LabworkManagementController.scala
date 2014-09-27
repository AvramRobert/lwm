package controllers

import controllers.StudentsManagement._
import models._
import org.joda.time.{ LocalDate, DateTime }
import play.api.mvc.{ Action, Controller }
import utils.Security.Authentication
import utils.semantic.Vocabulary.{ OWL, RDFS, LWM }
import utils.semantic._
import utils.Global._
import scala.concurrent.Future

/**
  * Created by rgiacinto on 20/08/14.
  */
object LabworkManagementController extends Controller with Authentication {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  def index() = hasPermissions(Permissions.AdminRole.permissions.toList: _*) { session ⇒
    Action.async { request ⇒
      for {
        courses ← Courses.all()
        labworks ← LabWorks.all()
        semesters ← Semesters.all()
      } yield {
        Ok(views.html.labwork_management(semesters.toList, labworks.toList, courses.toList, LabWorkForms.labworkForm))
      }
    }
  }

  def edit(labworkid: String) = hasPermissions(Permissions.AdminRole.permissions.toList: _*) { session ⇒
    Action.async { request ⇒
      for (assignments ← Assignments.all()) yield {
        val li = Individual(Resource(labworkid))
        val groups = li.props.getOrElse(LWM.hasGroup, List.empty[Resource]).map(r ⇒ Individual(r.asResource().get))
        val labworkAssignments = li.props.getOrElse(LWM.hasAssignmentAssociation, List.empty[Resource]).map(r ⇒ Individual(r.asResource().get))
        Ok(views.html.labWorkInformation(li, groups, labworkAssignments, assignments, AssignmentForms.assignmentAssociationForm))
      }
    }
  }

  def labWorkPost() = hasPermissions(Permissions.AdminRole.permissions.toList: _*) { session ⇒
    Action.async { implicit request ⇒
      LabWorkForms.labworkForm.bindFromRequest.fold(
        formWithErrors ⇒ {
          for {
            labworks ← LabWorks.all()
            courses ← Courses.all()
            semesters ← Semesters.all()
          } yield BadRequest(views.html.labwork_management(semesters.toList, labworks.toList, courses.toList, formWithErrors))
        },
        labwork ⇒ {
          LabWorks.create(
            LabWork(
              labwork.groupCount,
              labwork.assignmentCount,
              labwork.courseId,
              labwork.semester,
              new LocalDate(labwork.startDate.getTime),
              new LocalDate(labwork.endDate.getTime))).map { _ ⇒
              Redirect(routes.LabworkManagementController.index())
            }
        }
      )
    }
  }

  def labworkRemoval = hasPermissions(Permissions.AdminRole.permissions.toList: _*) { session ⇒
    Action.async(parse.json) { implicit request ⇒
      val id = (request.body \ "id").as[String]
      LabWorks.delete(Resource(id)).map { _ ⇒
        Redirect(routes.LabworkManagementController.index())
      }
    }
  }

  def setVisibility() = hasPermissions(Permissions.AdminRole.permissions.toList: _*) {
    session ⇒
      Action.async(parse.json) {
        implicit request ⇒
          val id = (request.body \ "id").as[String]
          val visibility = (request.body \ "visibility").as[String]
          val li = Individual(Resource(id))

          li.props(LWM.allowsApplications).map { e ⇒
            e.value match {
              case "true"  ⇒ li.update(LWM.allowsApplications, e, StringLiteral("false"))
              case "false" ⇒ li.update(LWM.allowsApplications, e, StringLiteral("true"))
            }
          }
          Future.successful(Redirect(routes.LabworkManagementController.index()))
      }
  }
}
