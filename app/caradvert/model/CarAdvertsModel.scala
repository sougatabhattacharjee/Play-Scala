package caradvert.model

import org.joda.time.LocalDate

/**
 * Created by Sougata on 10/14/2015.
 * Car DB models for new and used car
 */

abstract class CarAdvertsModel(id: Option[Int],
title: String,
fuel: Fuel,
price: Int,
newCar: Boolean,
mileage: Option[Int],
firstRegistration: Option[LocalDate]) {
  def withId(id: Int): CarAdvertsModel
  def getId(): Option[Int] = id
  def getTitle(): String = title
  def getFuel(): Fuel = fuel
  def getPrice(): Int = price
  def getType(): Boolean = newCar //if true then new car else used car
  def getMileage(): Option[Int] = mileage
  def getFirstRegistration(): Option[LocalDate] = firstRegistration
}

//Case class for new car, type is true
case class CarAdvertsNew(id: Option[Int],title: String,
                        fuel: Fuel, price: Int, newCar: Boolean)
  extends CarAdvertsModel(id, title, fuel, price, true, None, None) {
  override def withId(id: Int): CarAdvertsNew = CarAdvertsNew(Some(id), title, fuel, price, newCar)
}

//Case class for used car, type is false
case class CarAdvertsUsed(id: Option[Int], title: String,
                         fuel: Fuel, price: Int, newCar: Boolean,
                          mileage: Int, firstRegistration: LocalDate)
  extends CarAdvertsModel(id, title, fuel, price, false, Some(mileage), Some(firstRegistration)) {
  override def withId(id: Int): CarAdvertsUsed =
    CarAdvertsUsed(Some(id), title, fuel, price, newCar, mileage, firstRegistration)
}