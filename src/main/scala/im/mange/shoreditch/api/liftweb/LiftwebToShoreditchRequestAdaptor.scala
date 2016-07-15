package im.mange.shoreditch.api.liftweb

import im.mange.shoreditch.Shoreditch
import im.mange.shoreditch.handler.Request
import net.liftweb.common.Full
import net.liftweb.http.rest.RestHelper
import net.liftweb.http.{JsonResponse, LiftResponse, PlainTextResponse, Req}
import net.liftweb.json.Serialization._
import net.liftweb.json._

//TODO: and put the response stuff n here too ... so as little code as possible ...
case class LiftwebToShoreditchRequestAdaptor(req: Req) extends Request {
  val path = req match {
    case Req(x, _, _) ⇒ x.mkString("/")
    case _ ⇒ ""
  }

  val json = req.forcedBodyAsJson match {
    case Full(j) => {
      implicit val formats = Serialization.formats(NoTypeHints)
      write(j)
    }
    case _ => ""
  }

  def handle(shoreditch: Shoreditch) = Full(shoreditch.handle(this) match {
    case None => PlainTextResponse("Nothing to see here")
    case Some(j) => JsonResponse(parse(j))
  })

  override def toString = s"$path => ${json}"
}

case class ShoreditchRestHelper(shoreditch: Shoreditch) extends RestHelper {
  serve {
    case req @ (shoreditch.base :: extras) Any _ =>
      LiftwebToShoreditchRequestAdaptor(req).handle(shoreditch)
  }
}

protected object Any {
  def unapply(r: Req): Option[(List[String], Req)] =
    Some(r.path.partPath -> r)
}

