package caradvert.Persistence

import caradvert.model.{CarAdvertsUsed, CarAdvertsNew, CarAdvertsModel}
import com.google.inject.ImplementedBy

/**
 * Created by Sougata on 10/14/2015.
 */

@ImplementedBy(classOf[CarAdvertsDao])
trait CarAdvertsDaoImpl {

  def addCar(carAdvertsModel: CarAdvertsModel): CarAdvertsModel = carAdvertsModel match {
    case CarAdvertsNew(_,_,_,_,_) => addCar(carAdvertsModel.asInstanceOf[CarAdvertsNew])
    case CarAdvertsUsed(_,_,_,_,_,_,_) => addCar(carAdvertsModel.asInstanceOf[CarAdvertsUsed])
  }
  def addCar(carAdvertsNew: CarAdvertsNew): CarAdvertsModel
  def addCar(carAdvertsUsed: CarAdvertsUsed): CarAdvertsModel

}
