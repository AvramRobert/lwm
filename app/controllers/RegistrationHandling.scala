package controllers

/**
 * Created by root on 8/1/14.
 */

import akka.actor.{Props, Actor}
import akka.util.Timeout
import play.api.libs.concurrent.Akka
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future
import scala.util.Random


class RegistrationHandler extends Actor {
  var members = Map[String, Registration.Entry]()

  override def receive: Receive = {
    case Registration.Add(k, d) =>
      if(!members.contains(k)) {
        members += k -> d
        println("Added the member")
      }
    case Registration.CheckUser(user) => members.contains(user)
  }
}
object Registration extends Controller {
  import scala.concurrent.ExecutionContext.Implicits.global
  import play.api.Play.current

  case class Entry(matnr: Int, phone: String, address: String, city: String, plz: Int, mail: String, degree: String)
  case class Add(kennung: String, details: Entry)
  case class CheckUser(user: String)

  private val rh = Akka.system.actorOf(Props[RegistrationHandler], "registrationHandler")

  def addMember() = Action.async(parse.json) { request =>
    val id = request.session.get("session")
    val kennung = new Random().nextInt(2000).toString
    val matnr = (request.body \ "matrikelnummer").as[String]
    val phone = (request.body \ "telefon").as[String]
    val address = (request.body \ "anschrift").as[String]
    val city = (request.body \ "ort").as[String]
    val plz = (request.body \ "plz").as[String]
    val mail = (request.body \ "email").as[String]
    val degree = (request.body \ "degree").as[String]
    val e = rh ! Add(kennung, new Entry(matnr.toInt, phone, address, city, plz.toInt, mail, degree))

    Future {
      Ok(Json.obj("status" -> "OK"))
    }
  }

  def checkMember(user: String): Future[Boolean] = {
    import scala.concurrent.duration._
    import akka.pattern.ask
    import scala.concurrent.ExecutionContext.Implicits.global
    implicit val timeout = Timeout(15.seconds)
    
    for(s <- (rh ? CheckUser(user)).mapTo[Boolean])
      yield {
      s
    }
  }

  def removeMember(criteria: String, member: String): Unit = ???
}
