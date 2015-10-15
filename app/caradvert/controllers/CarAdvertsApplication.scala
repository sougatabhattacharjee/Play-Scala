package caradvert.controllers

import caradvert.Persistence.CarAdvertsDaoImpl
import com.google.inject.Inject
import play.Logger
import play.api.libs.json.{JsSuccess, JsError}
import play.api.mvc._
import play.libs.Json
import caradvert.model.{CarAdvertsModelFormatter, CarAdvertsModel}
import play.api.Play.current


class CarAdvertsApplication @Inject()(carAdvertsDaoImpl: CarAdvertsDaoImpl)
                                     (implicit carAdvertsModelFormatter: CarAdvertsModelFormatter) extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def list = TODO

  def listone(id: Int) = TODO

  def add = Action(parse.json) { request =>
    request.body.validate[CarAdvertsModel] match {
      case success: JsSuccess[CarAdvertsModel] =>
        Logger.info("One Car Added! " + success.get)
        Created(Json.toJson(carAdvertsDaoImpl.addCar(success.get)))
      case error: JsError =>
        BadRequest(JsError.toJson(error))
    }
  }

  def modify(id: Int) = TODO

  def delete(id: Int) = TODO

}