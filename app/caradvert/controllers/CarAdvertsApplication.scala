package caradvert.controllers

import javax.inject.Inject

import caradvert.Persistence.CarAdvertsDaoImpl
import caradvert.model.{CarAdvertsModel, CarAdvertsModelFormatter}
import play.Logger
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.{Action, Controller}

class CarAdvertsApplication @Inject()(carAdvertsDaoImpl: CarAdvertsDaoImpl)
                                     (implicit carAdvertsModelFormatter: CarAdvertsModelFormatter) extends Controller {

  def index = Action {
    Ok("Your new Car Adverts App is ready.")
  }

  //retrieve all cars with default sorting by id
  def list() = Action { request =>
    Ok(Json.toJson(carAdvertsDaoImpl.list(request.getQueryString("sortedBy"))))
  }

  def listOne(id: String) = Action {
    carAdvertsDaoImpl.listOne(id) match {
      case Some(advert) => Ok(Json.toJson(advert))
      case None => NotFound("No Car Found!")
    }
  }

  def add = Action(parse.json) { request =>
    request.body.validate[CarAdvertsModel] match {
      case success: JsSuccess[CarAdvertsModel] =>
        Logger.info("One Car Added! " + success.get)
        Created(Json.toJson(carAdvertsDaoImpl.addCar(success.get)))
      case error: JsError =>
        BadRequest(JsError.toJson(error))
    }
  }

  def delete(id: String) = Action {
    carAdvertsDaoImpl.carDelete(id) match {
      case 1 =>
        Logger.info("Deleted Car :" + id)
        carAdvertsDaoImpl.carDelete(id)
        Ok("Deleted Car :" + id)
      case 0 => NotFound("No Car Found!")
    }
  }

  def modify(id: String) = Action(parse.json) { request =>
    request.body.validate[CarAdvertsModel] match {
      case (success: JsSuccess[CarAdvertsModel]) =>
          Logger.info("Car Modifying " + success.get)
          Ok(Json.toJson(carAdvertsDaoImpl.modify(success.get.withId(id))))
      case (error: JsError) =>
        BadRequest(JsError.toJson(error))
    }
  }

}