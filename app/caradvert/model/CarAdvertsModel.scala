package caradvert.model

import java.time.LocalDate

/**
 * Created by Sougata on 10/14/2015.
 * Car DB models for new and used car
 */

abstract class CarAdvertsModel(id: Option[String],
title: String,
fuel: Fuel,
price: Int,
newCar: Boolean,
mileage: Option[Int],
firstRegistration: Option[LocalDate]) {
  def withId(id: String): CarAdvertsModel
  def getId(): Option[String] = id
  def getTitle(): String = title
  def getFuel(): Fuel = fuel
  def getPrice(): Int = price
  def getType(): Boolean = newCar //if true then new car else used car
  def getMileage(): Option[Int] = mileage
  def getFirstRegistration(): Option[LocalDate] = firstRegistration
}

//Case class for new car, type is true
case class CarAdvertsNew(id: Option[String],title: String,
                        fuel: Fuel, price: Int, newCar: Boolean)
  extends CarAdvertsModel(id, title, fuel, price, true, None, None) {

  override def withId(id: String): CarAdvertsNew = CarAdvertsNew(Some(id), title, fuel, price, newCar)
  def randomUUID = java.util.UUID.randomUUID().toString
}

//Case class for used car, type is false
case class CarAdvertsUsed(id: Option[String], title: String,
                         fuel: Fuel, price: Int, newCar: Boolean,
                          mileage: Int, firstRegistration: LocalDate)
  extends CarAdvertsModel(id, title, fuel, price, false, Some(mileage), Some(firstRegistration)) {
  override def withId(id: String): CarAdvertsUsed =
    CarAdvertsUsed(Some(id), title, fuel, price, newCar, mileage, firstRegistration)
  def randomUUID = java.util.UUID.randomUUID().toString
}

