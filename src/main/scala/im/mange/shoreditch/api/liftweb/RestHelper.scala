package im.mange.shoreditch.api.liftweb

import im.mange.shoreditch.api.{Action, Check}
import net.liftweb.common.Box
import net.liftweb.http._
import net.liftweb.common.Full
import scala.Some

//TODO: is this still needed?
trait RestHelper extends net.liftweb.http.rest.RestHelper {
  def runAction(a: ⇒ Action, req: Request): () ⇒ Box[LiftResponse] = { () ⇒ doRun(a, req) }
  def runCheck(c: ⇒ Check): () ⇒ Box[LiftResponse] = { () ⇒ doRun(c) }

  private def doRun(a: => Action, req: Request): Full[LiftResponse] = {
    Full(JsonResponse(Json.serialise(Runner.run(a, req))))
  }

  private def doRun(c: => Check): Full[LiftResponse] = {
    Full(JsonResponse(Json.serialise(Runner.run(c))))
  }

  object GET {
    def unapplySeq(in: Req): Option[Seq[String]] = in match {
      case Req(out, _, GetRequest) ⇒ Some(out)
      case _ ⇒ None
    }
  }

  object POST {
    def unapplySeq(in: Req): Option[Seq[String]] = in match {
      case Req(out, _, PostRequest) ⇒ Some(out)
      case _ ⇒ None
    }
  }
}
