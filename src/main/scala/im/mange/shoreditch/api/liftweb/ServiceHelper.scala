package im.mange.shoreditch.api.liftweb

import im.mange.shoreditch.api._
import im.mange.shoreditch.api.liftweb.EnhancedRestHelper._
import net.liftweb.http.{JsonResponse, LiftResponse, Req}
import net.liftweb.common.{Box, Full}

object ServiceHelper {
  implicit class CheckRouteBuildingString(val path: String) extends AnyVal {
    def action(probeFn:               ⇒ Action): Route[Service] = POST0("action/" + path)(probeFn)
    def check(probeFn:                 ⇒ Check): Route[Service] = GET0("check/" + path)(probeFn)
    def check(probeFn: (String)        ⇒ Check): Route[Service] = GET1("check/" + path)(probeFn)
    def check(probeFn: (String,String) ⇒ Check): Route[Service] = GET2("check/" + path)(probeFn)
  }
}

abstract class ServiceHelper(base: String = "", version: String)(offerings: Route[Service]*)
  extends EnhancedRestHelper[Service](base, "metadata", version)(offerings: _*) {

  def xform(req: Req) = mkRunFunc(_, req)

  private def mkRunFunc(t: Service, req: Req): () ⇒ Box[LiftResponse] = () ⇒ {
    t match {
      case a:Action ⇒ Full(JsonResponse(Json.serialise(Runner.run(a, req))))
      case c:Check ⇒ Full(JsonResponse(Json.serialise(Runner.run(c))))
      case x => throw new RuntimeException("I don't know how to run a: " + x)
    }
  }
}
