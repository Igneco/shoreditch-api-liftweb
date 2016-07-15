package im.mange.shoreditch.api.liftweb

import net.liftweb.http.Req
import net.liftweb.json.JValue

//TODO: move all of this back a level
trait Request {
  //TODO: ultimately this needs to be a String
  val json: JValue
  //TODO: ultimately this needs to be a String
  val inboundPathParts: Seq[String]
}

case class LiftwebRequest(req: Req) extends Request {
  val json = if (req.json_?) req.json.getOrElse("") else req.forcedBodyAsJson..getOrElse("")

  val inboundPathParts = req match {
    case Req(x, _, _) ⇒ println(s"params: ${req.params}"); x
    case _ ⇒ Nil

  }
}

case class SimpleRequest(json: JValue, inboundPathParts: Seq[String]) extends Request {
}
