package im.mange.shoreditch.api.liftweb

import im.mange.shoreditch.api.Request
import net.liftweb.common.Full
import net.liftweb.http.Req
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
      val theFormats = Serialization.formats(NoTypeHints)
      implicit val formats = theFormats
      write(j)
    }
    case _ => ""
  }

  override def toString = s"$path => ${json}"

//  def handle(shoreditch: Shoreditch) = {
//    val handler = Booking.handler(request)
//    //      val handler = Cleared.handler(new LiftwebRequest(req))
//    println(handler)
//
//    val response: LiftResponse = handler match {
//      case None => PlainTextResponse("Nothing to see here")
//      case Some(j) => JsonResponse(parse(j()))
//    }
//    Full(response)
//  }

}




