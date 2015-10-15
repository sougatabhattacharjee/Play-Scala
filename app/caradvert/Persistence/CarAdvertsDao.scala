package caradvert.Persistence

import java.sql.{SQLException, ResultSet, Statement, PreparedStatement}

import caradvert.model.{CarAdvertsModel, CarAdvertsNew, CarAdvertsUsed}
import com.google.inject.Inject
import play.api.db.DB
import play.api.Play.current

/**
 * Created by Sougata on 10/14/2015.
 */
class CarAdvertsDao @Inject()() extends CarAdvertsDaoImpl {

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
}
