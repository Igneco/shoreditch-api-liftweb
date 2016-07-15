package im.mange.shoreditch.api.liftweb

import net.liftweb.json._
import net.liftweb.json.Serialization._
import im.mange.shoreditch.api.MetaDataResponse
import im.mange.shoreditch.api.ActionResponse
import im.mange.shoreditch.api.In
import im.mange.shoreditch.api.CheckResponse

object Json {
  private val theFormats = Serialization.formats(NoTypeHints)

  def serialise(r: ActionResponse) = {
    implicit val formats = theFormats
    JsonParser.parse(write(r))
  }

  def serialise(r: CheckResponse) = {
    implicit val formats = theFormats
    JsonParser.parse(write(r))
  }

  def serialise(r: MetaDataResponse): JValue = {
    implicit val formats = theFormats
    JsonParser.parse(write(r))
  }

  def deserialiseIn(json: JValue) = {
    implicit val formats = theFormats
    json.extract[List[In]]
  }
}
