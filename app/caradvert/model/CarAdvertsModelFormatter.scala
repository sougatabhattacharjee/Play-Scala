package caradvert.model

/**
 * Created by Sougata on 10/15/2015.
 */


import java.time.LocalDate
import javax.inject.Singleton
import play.api.libs.functional.syntax._
import play.api.libs.json._



@Singleton
class CarAdvertsModelFormatter extends Format[CarAdvertsModel] {

  def fuelFrom(fuel: String): Fuel =
    if (fuel == Diesel.toString()) Diesel
    else if (fuel == Gasoline.toString()) Gasoline
    else throw new IllegalStateException("Unknown fuel " + fuel)

  private implicit val fuelFormatter: Format[Fuel] = new Format[Fuel] {

    override def reads(json: JsValue): JsResult[Fuel] = {
      json match {
        case JsString(fuel) => fuel match {
          case "Diesel" => JsSuccess(Diesel)
          case "Gasoline" => JsSuccess(Gasoline)
          case _ => jsFuelError("unknown fuel '" + fuel + "'!")
        }
        case _ => jsFuelError("Fuel should be a String!")
      }
    }
    override def writes(fuel: Fuel): JsValue = JsString(fuel.toString())
    private def jsFuelError(concreteError: String): JsError =
      JsError("fuel " + concreteError + " should be either '" + Diesel.toString + "' or '" + Gasoline.toString + "'")
}



  private val commonFormatBuilder =
    (JsPath \ "id").formatNullable[String] and
      (JsPath \ "title").format[String] and
      (JsPath \ "fuel").format[Fuel] and
      (JsPath \ "price").format[Int] and
      (JsPath \ "new").format[Boolean]

  private val adForNewFormat: Format[CarAdvertsNew] =
    commonFormatBuilder(CarAdvertsNew.apply, unlift(CarAdvertsNew.unapply))

  private val adForUsedFormat: Format[CarAdvertsUsed] = (
  commonFormatBuilder and
  (JsPath \ "mileage").format[Int] and
  (JsPath \ "firstRegistration").format[LocalDate]
  )(CarAdvertsUsed.apply, unlift(CarAdvertsUsed.unapply))


  override def writes(ad: CarAdvertsModel): JsValue = {
    ad match {
      case CarAdvertsNew(_,_,_,_,_) =>
        adForNewFormat.writes(ad.asInstanceOf[CarAdvertsNew]).as[JsObject]
      case CarAdvertsUsed(_,_,_,_,_,_,_) =>
        adForUsedFormat.writes(ad.asInstanceOf[CarAdvertsUsed]).as[JsObject]
    }
  }

  override def reads(json: JsValue): JsResult[CarAdvertsModel] = {
    json match {
      case JsObject(_) => reads(json.as[JsObject])
      case _ => JsError("CarAdvertsModel must be an object")
    }
  }

  private def reads(json: JsObject): JsResult[CarAdvertsModel] = {
    val carType: JsValue = json.fields.find(field => field._1 == "new").getOrElse("new" -> JsNull)._2
    carType match {
      case JsNull => jsTypeError("CarType is required, either true or false")
      case JsBoolean(value) => value match {
        case true => adForNewFormat.reads(json - "true")
        case false => adForUsedFormat.reads(json - "false")
        case _ => jsTypeError("has invalid value either true or false '" + value + "'")
      }
      case _ => jsTypeError("Unknown Error")
    }
  }
  private def jsTypeError(concreteError: String): JsError =
    JsError("'new' " + concreteError + ". Must be either false or true")
}
