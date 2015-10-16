package caradvert.Persistence

import caradvert.model.{CarAdvertsModel, CarAdvertsNew, CarAdvertsUsed}
import com.google.inject.ImplementedBy

/**
 * Created by Sougata on 10/14/2015.
 */

@ImplementedBy(classOf[CarAdvertsDao])
trait CarAdvertsDaoImpl {

  def list(): List[CarAdvertsModel] = list(None)
  def list(sortBy: Option[String]): List[CarAdvertsModel] = list(sortBy.getOrElse("id"))
  def list(sortBy: String): List[CarAdvertsModel]

  def listOne(maybeId: Option[String]): Option[CarAdvertsModel] = maybeId match {
    case Some(id) => listOne(id)
    case None => None
  }
  def listOne(id: String): Option[CarAdvertsModel]


  def addCar(carAdvertsModel: CarAdvertsModel): CarAdvertsModel = carAdvertsModel match {
    case CarAdvertsNew(_,_,_,_,_) => addCar(carAdvertsModel.asInstanceOf[CarAdvertsNew])
    case CarAdvertsUsed(_,_,_,_,_,_,_) => addCar(carAdvertsModel.asInstanceOf[CarAdvertsUsed])
  }
  def addCar(carAdvertsNew: CarAdvertsNew): CarAdvertsModel
  def addCar(carAdvertsUsed: CarAdvertsUsed): CarAdvertsModel


  def carDelete(id: String): Int

  def modify(carAdvertsModel: CarAdvertsModel): CarAdvertsModel


}
