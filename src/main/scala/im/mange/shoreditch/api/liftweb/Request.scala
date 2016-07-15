package im.mange.shoreditch.api.liftweb

import net.liftweb.common.Full
import net.liftweb.http.Req
import net.liftweb.json.JValue
import net.liftweb.json._
import net.liftweb.json.Serialization._


//TODO: move all of this back a level
trait Request {
  //TODO: ultimately this needs to be a String
  val json: String
  //TODO: ultimately this needs to be a String
  val inboundPathParts: Seq[String]
}

//TODO: rename to LiftwebToShoreditchRequestAdaptor
//TODO: and put the response stuff n here too ... so as little code as possible ...
case class LiftwebRequest(req: Req) extends Request {
//  val json = if (req.json_?) req.json.getOrElse("") else req.forcedBodyAsJson.getOrElse("")
  val json = req.forcedBodyAsJson match {
    case Full(j) => {
      val theFormats = Serialization.formats(NoTypeHints)
      implicit val formats = theFormats
      write(j)
    }
    case _ => ""
  }

  val inboundPathParts = req match {
    case Req(x, _, _) ⇒ x
    case _ ⇒ Nil
  }
}

case class SimpleRequest(json: String, inboundPathParts: Seq[String]) extends Request {
}
