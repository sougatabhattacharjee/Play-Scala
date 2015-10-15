package caradvert.Persistence

import java.sql.{SQLException, ResultSet, Statement, PreparedStatement}
import java.text.SimpleDateFormat
import java.time.{Instant, LocalDate, ZoneId}
import java.util.Date

import caradvert.model._
import com.google.inject.Inject
import org.joda.time.DateTime
import play.api.db.DB
import play.api.Play.current

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
      insert.setInt(5, 1)
      insert
    }
    )
  }

  private def withAdvertCreation(carAdvertsModel: CarAdvertsModel,
                                 addSql: String,
                                 paramsSetter: PreparedStatement => PreparedStatement): CarAdvertsModel = {
    DB.withConnection { conn =>
      val insert: PreparedStatement =
        paramsSetter(conn.prepareStatement(addSql, Statement.RETURN_GENERATED_KEYS))
      val affectedRows: Int = insert.executeUpdate()
      val generatedKeys: ResultSet = insert.getGeneratedKeys()
      if (affectedRows == 0) throw new SQLException("Adding car advert failed: no rows affected!")
      else if (!generatedKeys.next()) throw new SQLException("Adding car advert failed: no id returned!")
      else return carAdvertsModel.withId(generatedKeys.getString(1))
    }
  }


  override def addCar(carAdvertsUsed: CarAdvertsUsed): CarAdvertsModel = ???



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
        resultSet.getString("ID"),
        resultSet.getString("TITLE"),
        carAdvertsModelFormatter.fuelFrom(resultSet.getString("FUEL")),
        resultSet.getInt("PRICE"),
        true
      )

    else if (carType == 0)
      CarAdvertsUsed(
        resultSet.getString("ID"),
        resultSet.getString("TITLE"),
        carAdvertsModelFormatter.fuelFrom(resultSet.getString("FUEL")),
        resultSet.getInt("PRICE"),
        false,
        resultSet.getInt("MILEAGE"),
        toLocalDate(resultSet.getString("FIRSTRREGISTRATION"))
      )

    else throw new IllegalStateException("Unknown car Type " + carType)
  }

  def toLocalDate(s: String): LocalDate =
    new SimpleDateFormat("yyyy-MM-dd").parse(s).toInstant().atZone(ZoneId.systemDefault()).toLocalDate;

}
