package caradvert.controllers

import play.api.mvc._

object CarAdvertsApplication extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def list = TODO

  def list(id: Int) = TODO

  def add = TODO

  def modify(id: Int) = TODO

  def delete(id: Int) = TODO

}