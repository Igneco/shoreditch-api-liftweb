package im.mange.shoreditch.api.liftweb

import net.liftweb.http.Req
import net.liftweb.json.JValue

trait Request {
  //TODO: ultimately this needs to be a String
  val json: JValue
}

case class LiftwebRequest(req: Req) extends Request {
  val json = if (req.json_?) req.json.get else req.forcedBodyAsJson.get
}

case class SimpleRequest(json: JValue) extends Request {
}
