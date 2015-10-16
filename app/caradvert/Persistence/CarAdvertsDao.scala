package caradvert.Persistence

import java.sql._
import java.text.SimpleDateFormat
import java.time.{LocalDate, ZoneId}
import java.util
import java.util.Calendar

import anorm._
import caradvert.model._
import com.google.inject.Inject
import play.api.Play.current
import play.api.db.DB

/**
 * Created by Sougata on 10/14/2015.
 */
class CarAdvertsDao @Inject()(carAdvertsModelFormatter : CarAdvertsModelFormatter) extends CarAdvertsDaoImpl {

  override def addCar(carAdvertsNew: CarAdvertsNew): CarAdvertsModel = {
    lazy val id = carAdvertsNew.randomUUID
    withAdvertCreation(
    carAdvertsNew,
    "insert into CARADVERTS (ID, TITLE, FUEL, PRICE, NEWCAR) values (?, ?, ?, ?, ?)",
    { insert: PreparedStatement =>
      insert.setString(1, id)
      insert.setString(2, carAdvertsNew.title)
      insert.setString(3, carAdvertsNew.fuel.toString())
      insert.setInt(4, carAdvertsNew.price)
      insert.setInt(5, 1) ///new car
      insert
    },
    id
    )
  }

  private def withAdvertCreation(carAdvertsModel: CarAdvertsModel,
                                 addSql: String,
                                 paramsSetter: PreparedStatement => PreparedStatement, id: String): CarAdvertsModel = {
    DB.withConnection { conn =>
      val insert: PreparedStatement =
        paramsSetter(conn.prepareStatement(addSql, Statement.RETURN_GENERATED_KEYS))
      val affectedRows: Int = insert.executeUpdate()
      val generatedKeys: ResultSet = insert.getGeneratedKeys()
      if (affectedRows == 0) throw new SQLException("Adding new car failed: no rows affected!")
      else return carAdvertsModel.withId(id)
    }
  }


  override def addCar(carAdvertsUsed: CarAdvertsUsed): CarAdvertsModel = {
    lazy val id = carAdvertsUsed.randomUUID
    withAdvertCreation(
    carAdvertsUsed,
    "insert into CARADVERTS (ID, TITLE, FUEL, PRICE, NEWCAR,MILEAGE,FIRSTRREGISTRATION) values (?, ?, ?, ?, ?, ?, ?)",
    { insert: PreparedStatement =>
      insert.setString(1, id)
      insert.setString(2, carAdvertsUsed.title)
      insert.setString(3, carAdvertsUsed.fuel.toString())
      insert.setInt(4, carAdvertsUsed.price)
      insert.setInt(5, 0) //used car
      insert.setInt(6, carAdvertsUsed.getMileage().get)
      insert.setDate(7, new java.sql.Date(toDate(carAdvertsUsed.getFirstRegistration().get).getTime))
      insert
    },
    id
    )
  }



  override def listOne(id: String): Option[CarAdvertsModel] = {
    DB.withConnection { conn =>
      val listOne: PreparedStatement =
        conn.prepareStatement("select ID,TITLE,FUEL,PRICE,NEWCAR,MILEAGE,FIRSTRREGISTRATION from CARADVERTS where ID = ?")
      listOne.setString(1, id)
      val adverts = returnResultSet(listOne.executeQuery())
      if (adverts.size == 1) Some(adverts.head)
      else None
    }
  }

  override def list(sortBy: String): List[CarAdvertsModel] = {
    assume(sortBy.matches("^[a-zA-Z0-9]*$"), "Invalid sort pattern")
    DB.withConnection { conn =>
      val list: PreparedStatement =
        conn.prepareStatement("select ID,TITLE,FUEL,PRICE,NEWCAR,MILEAGE,FIRSTRREGISTRATION from CARADVERTS order by " + sortBy)

      val resultSet = list.executeQuery()

      returnResultSet(resultSet)
    }
  }
  private def returnResultSet(resultSet: ResultSet): List[CarAdvertsModel] =
    if (!resultSet.next()) List()
    else createOneRow(resultSet)::returnResultSet(resultSet)


  private def createOneRow(resultSet: ResultSet): CarAdvertsModel = {
    val carType: Int = resultSet.getInt("NEWCAR")

    if (carType == 1)
      CarAdvertsNew(
        Some(resultSet.getString("ID")),
        resultSet.getString("TITLE"),
        carAdvertsModelFormatter.fuelFrom(resultSet.getString("FUEL")),
        resultSet.getInt("PRICE"),
        true
      )

    else if (carType == 0)
      CarAdvertsUsed(
        Some(resultSet.getString("ID")),
        resultSet.getString("TITLE"),
        carAdvertsModelFormatter.fuelFrom(resultSet.getString("FUEL")),
        resultSet.getInt("PRICE"),
        false,
        resultSet.getInt("MILEAGE"),
        toLocalDate(resultSet.getString("FIRSTRREGISTRATION"))
      )

    else throw new IllegalStateException("Unknown car Type " + carType)
  }


 override def carDelete(id: String): Int = {
    DB.withConnection { implicit c =>
      val nRowsDeleted = SQL("DELETE FROM CARADVERTS WHERE id = {id}")
        .on('id -> id)
        .executeUpdate()
      nRowsDeleted
    }
  }

  override def modify(carAdvertsModel: CarAdvertsModel): CarAdvertsModel = {
    DB.withConnection { conn =>
      val update: PreparedStatement = conn.prepareStatement(
        "update CARADVERTS set TITLE = ?, FUEL = ?, PRICE = ?, MILEAGE = ?, FIRSTRREGISTRATION = ? where ID = ?"
      )
      update.setString(1, carAdvertsModel.getTitle())
      update.setString(2, carAdvertsModel.getFuel().toString())
      update.setInt(3, carAdvertsModel.getPrice())
      carAdvertsModel.getMileage() match {
        case Some(mileage) => update.setInt(4, mileage)
        case None => update.setNull(4, Types.INTEGER)
      }
      carAdvertsModel.getFirstRegistration() match {
        case Some(date) => update.setDate(5, new java.sql.Date(toDate(carAdvertsModel.getFirstRegistration().get).getTime))
        case None => update.setNull(5, Types.DATE)
      }
      update.setString(6, carAdvertsModel.getId().get)
      if (update.executeUpdate() == 0) throw new NoSuchElementException("Update failed!")
      else return carAdvertsModel
    }
  }

  def toDate(localDate: LocalDate): util.Date =
  new SimpleDateFormat("yyyy-MM-dd").parse(localDate.toString())

  def dateToCurrentDate(): util.Date =
  new SimpleDateFormat("yyyy-MM-dd").parse(Calendar.getInstance().getTime().toString)
  //format.format(Calendar.getInstance().getTime()).toString

  def toLocalDate(s: String): LocalDate =
    new SimpleDateFormat("yyyy-MM-dd").parse(s).toInstant().atZone(ZoneId.systemDefault()).toLocalDate;

  override def clearTable(): Unit = {
    DB.withConnection { conn =>
      conn.prepareStatement("delete from CARADVERTS").execute()
    }
  }

}
