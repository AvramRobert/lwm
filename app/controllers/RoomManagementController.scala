package controllers

import akka.actor.ActorSystem
import controllers.LabworkManagementController._
import models._
import play.api.Play
import play.api.mvc.{ Action, Controller }
import play.libs.Akka
import utils.Security.Authentication
import utils.TransactionSupport
import utils.semantic.Vocabulary.{ rdfs, lwm }
import utils.semantic.{ StringLiteral, Individual, Resource }
import utils.Global._
import scala.concurrent.Future

/**
  * Room Management:
  *
  */
object RoomManagementController extends Controller with Authentication with TransactionSupport {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._
  import Play.current
  override def system: ActorSystem = Akka.system()

  def index() = hasPermissions(Permissions.AdminRole.permissions.toList: _*) { session ⇒
    Action.async { implicit request ⇒
      for {
        rooms ← Rooms.all()
      } yield {
        Ok(views.html.room_management(rooms, Rooms.Forms.roomForm))
      }
    }
  }

  def roomPost = hasPermissions(Permissions.AdminRole.permissions.toList: _*) { session ⇒
    Action.async { implicit request ⇒
      Rooms.Forms.roomForm.bindFromRequest.fold(
        formWithErrors ⇒ {
          for (all ← Rooms.all()) yield {
            BadRequest(views.html.room_management(all.toList, formWithErrors))
          }
        },
        room ⇒ {
          Rooms.create(Room(room.roomId, room.name)).map { i ⇒
            createTransaction(session.user, i.uri, s"Room ${i.uri} created by ${session.user}")
            Redirect(routes.RoomManagementController.index())
          }
        }
      )
    }
  }

  def roomRemoval() = hasPermissions(Permissions.AdminRole.permissions.toList: _*) {
    session ⇒
      Action.async(parse.json) {
        implicit request ⇒
          val id = (request.body \ "id").as[String]
          Rooms.delete(Resource(id)).map { r ⇒
            deleteTransaction(session.user, r, s"Room $r removed by ${session.user}")
            Redirect(routes.RoomManagementController.index())
          }
      }
  }

  def roomEdit(roomId: String) = hasPermissions(Permissions.AdminRole.permissions.toList: _*) {
    session ⇒
      Action.async { implicit request ⇒
        val i = Individual(Resource(roomId))
        Rooms.Forms.roomForm.bindFromRequest.fold(
          formWithErrors ⇒ {
            for (all ← Rooms.all()) yield {
              BadRequest(views.html.room_management(all.toList, formWithErrors))
            }
          },
          room ⇒ {
            for {
              id ← i.props(lwm.hasRoomId)
              name ← i.props(lwm.hasName)
            } yield {
              i.update(lwm.hasRoomId, id, StringLiteral(room.roomId))
              i.update(lwm.hasName, name, StringLiteral(room.name))
              i.update(rdfs.label, name, StringLiteral(room.name))
              modifyTransaction(session.user, i.uri, s"Room ${i.uri} modified by ${session.user}")
            }
            Future.successful(Redirect(routes.RoomManagementController.index()))
          }
        )
      }
  }

}
