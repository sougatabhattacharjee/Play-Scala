package caradvert

import caradvert.Persistence.{CarAdvertsDaoImpl, CarAdvertsDao}
import caradvert.model.{CarAdvertsNew, CarAdvertsModel, CarAdvertsModelFormatter}
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.Logger
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test._

/**
 * Created by Sougata on 10/15/2015.
 */
 @RunWith(classOf[JUnitRunner])
  class CarAdvertsApplicationTestSpec extends Specification {

    private implicit val formatter = new CarAdvertsModelFormatter()
  private val carAdvertsDao: CarAdvertsDaoImpl = new CarAdvertsDao(formatter)

    val validBodies = List(
      JsObject(Map(
        "title" -> JsString("Audi A4"),
        "price" -> JsNumber(1000),
        "fuel" -> JsString("Gasoline"),
         "new" -> JsBoolean(true)
      )),
      JsObject(Map(
        "title" -> JsString("Toyota"),
        "price" -> JsNumber(2000),
        "fuel" -> JsString("Diesel"),
        "new" -> JsBoolean(false),
        "mileage" -> JsNumber(28),
        "firstRegistration" -> JsString("2015-10-09")
      ))
    )

  val invalidBodies = List(
    "{invalid: json}",
    """{ "id": "ccc"}""",
    """{ "fuel": "Gas"}"""
  )


    "Application" should {

      "send 415 unsupported media type when creating car adverts with no JSON content type" in new WithApplication() {
        val resp = route(FakeRequest(POST, "/caradv")).get
        status(resp) must equalTo(UNSUPPORTED_MEDIA_TYPE)
      }

      "send 400 bad request when creating an advert with invalid JSON body" in new WithApplication {
        invalidBodies.foreach { invalidBody =>
          val resp = route(
            FakeRequest(POST, "/caradv")
              .withHeaders(("Content-Type", "application/json"))
              .withTextBody(invalidBody)
          ).get
          Logger.info("Received after invalid JSON body: " + contentAsString(resp))
          status(resp) must equalTo(BAD_REQUEST)
        }
      }

      "send 400 bad request when creating a car advert with no JSON body" in new WithApplication {
        val resp = route(
          FakeRequest(POST, "/caradv")
            .withHeaders(("Content-Type", "application/json"))
        ).get
        status(resp) must equalTo(BAD_REQUEST)
      }



      "send created success with the created car advert details when valid JSON body provided" in new WithApplication {
        carAdvertsDao.clear()
        validBodies.foreach { validBody =>
          val resp = route(
            FakeRequest(POST, "/caradv")
              .withHeaders(("Content-Type", "application/json"))
              .withJsonBody(validBody)
          ).get
          Logger.info("Received after valid JSON body: " + contentAsJson(resp))
          status(resp) must equalTo(CREATED)
          val response: JsObject = Json.parse(contentAsString(resp)).as[JsObject]
          response - "id" must beEqualTo(validBody)
        }
      }


      "send all records sorted by id by default" in new WithApplication {
        val resp = route(FakeRequest(GET, "/caradv")).get
        status(resp) must equalTo(OK)
        val response: JsArray = Json.parse(contentAsString(resp)).as[JsArray]
        response(0).validate[CarAdvertsModel].get.getTitle() must beEqualTo("Audi A4")
        response(1).validate[CarAdvertsModel].get.getTitle() must beEqualTo("Toyota")
      }



      "records sorted by the specified criteria" in new WithApplication {
        val resp = route(FakeRequest(GET, "/caradv?sortedBy=price")).get
        status(resp) must equalTo(OK)
        val response: JsArray = Json.parse(contentAsString(resp)).as[JsArray]
        // title ordered
        response(1).validate[CarAdvertsModel].get.getTitle() must beEqualTo("Audi A4")
        response(0).validate[CarAdvertsModel].get.getTitle() must beEqualTo("Toyota")
      }


      // getting a single record
      "send OK if car exists with details" in new WithApplication {
        private val advert: CarAdvertsModel = carAdvertsDao.list().head
        private val resp = route(FakeRequest(GET, "/caradv/" + advert.getId().get)).get
        status(resp) must equalTo(OK)
        contentAsJson(resp).validate[CarAdvertsModel].get must beEqualTo(advert)
      }

      "send 404 No Car found status without content if a car is not exists" in new WithApplication {
        private val resp = route(FakeRequest(GET, "/caradv/" + 6209940e56)).get
        status(resp) must equalTo(NOT_FOUND)
        contentAsString(resp).length must beEqualTo(0)
      }


      //updating
      "send 415 unsupported media type when updating with wrong media type" in new WithApplication {
        private val resp = route(FakeRequest(PUT, "/caradv/" + 6209940e56)).get
        status(resp) must equalTo(UNSUPPORTED_MEDIA_TYPE)
      }

      "send not found without body when updating a non existent car" in new WithApplication {
        private val carAdvertsNew: CarAdvertsModel = carAdvertsDao.list().find { _.isInstanceOf[CarAdvertsNew] }.get
        private val updatedAdvertForNew: CarAdvertsModel = CarAdvertsNew(
          None, "Updated title", carAdvertsNew.getFuel(), carAdvertsNew.getPrice() * 2, true
        )
        private val resp = route(
          FakeRequest(PUT, "/caradv/" + 30403)
            .withHeaders(("Content-Type", "application/json"))
            .withJsonBody(Json.toJson(updatedAdvertForNew))
        ).get
        status(resp) must equalTo(NOT_FOUND)
        contentAsString(resp).length must beEqualTo(0)
      }
      "send OK along with a valid body when updating a existent advert" in new WithApplication {
        private val carAdvertsNew: CarAdvertsModel = carAdvertsDao.list().find { _.isInstanceOf[CarAdvertsNew] }.get
        private val updatedAdvertForNew: CarAdvertsModel = CarAdvertsNew(
          carAdvertsNew.getId(), "Updated title", carAdvertsNew.getFuel(), carAdvertsNew.getPrice() * 2, true
        )
        private val resp = route(
          FakeRequest(PUT, "/caradv/" + carAdvertsNew.getId().get)
            .withHeaders(("Content-Type", "application/json"))
            .withJsonBody(Json.toJson(updatedAdvertForNew))
        ).get
        status(resp) must equalTo(OK)
        contentAsJson(resp).validate[CarAdvertsModel].get must beEqualTo(updatedAdvertForNew)
      }


      //deleting
      "send 404 No Car found status without content if a car is not exists when deleting" in new WithApplication {
        private val resp = route(FakeRequest(DELETE, "/caradv/" + 6209940e56)).get
        status(resp) must equalTo(NOT_FOUND)
        contentAsString(resp).length must beEqualTo(0)
      }

      "send no content after deleting an existing car" in new WithApplication {
        private val resp = route(FakeRequest(DELETE, "/caradv/" + "0fe4ec90-9186-47cc-990d-e68a0ac261ba")).get
        status(resp) must equalTo(NOT_FOUND)
        contentAsString(resp).length must beEqualTo(0)
      }
    }
}
