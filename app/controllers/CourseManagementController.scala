package controllers

import models.{DegreeForms, CourseForms, Courses, Degrees}
import play.api.mvc.{Action, Controller}
import utils.Security.Authentication

import scala.concurrent.{Future, ExecutionContext}

/**
 * Created by rgiacinto on 21/08/14.
 */
object CourseManagementController extends Controller with Authentication{
  import ExecutionContext.Implicits.global

  def index() = hasPermissions(Permissions.AdminRole.permissions.toList: _*){session =>
    Action.async { request =>
      for{
        courses <- Courses.all()
      } yield{
        Ok(views.html.courseManagement(courses.toList, CourseForms.courseForm))
      }
    }
  }

  def coursePost() = hasPermissions(Permissions.AdminRole.permissions.toList: _*) { session =>
    Action.async { implicit request =>
      CourseForms.courseForm.bindFromRequest.fold(
        formWithErrors => {
          for {
            degrees <- Degrees.all()
          } yield {
            BadRequest(views.html.courseManagement(degrees.toList, formWithErrors))
          }
        },
        course => {
          Courses.create(course)
          Future.successful(Redirect(routes.CourseManagementController.index()))
        }
      )
    }
  }
}