package im.mange.shoreditch.api.liftweb

import im.mange.shoreditch.api._
import im.mange.shoreditch.api.liftweb.EnhancedRestHelper._
import net.liftweb.http.{JsonResponse, LiftResponse, Req}
import net.liftweb.common.{Box, Full}

object ServiceHelper {
  //TODO: if this works, add boolean generateOptionsForCors
  implicit class CheckRouteBuildingString(val path: String) extends AnyVal {
    def action(probeFn:               ⇒ Action): Route[Service] = //List(
      POST0("action/" + path)(probeFn)//,
      //OPTIONS0("action/" + path)(probeFn)
    //)
    def check(probeFn:                 ⇒ Check): Route[Service] = GET0("check/" + path)(probeFn)
    def check(probeFn: (String)        ⇒ Check): Route[Service] = GET1("check/" + path)(probeFn)
    def check(probeFn: (String,String) ⇒ Check): Route[Service] = GET2("check/" + path)(probeFn)
  }
}

abstract class ServiceHelper(longName: String, alias: String, base: String, version: String, checksEnabled: Boolean, actionsEnabled: Boolean)(offerings: Route[Service]*)
  extends EnhancedRestHelper[Service](longName, alias, base, "metadata", version)(offerings: _*) {

  def xform(req: Request): (Service) => () => Box[LiftResponse] = mkRunFunc(_, req)

  private def mkRunFunc(t: Service, req: Request): () ⇒ Box[LiftResponse] = () ⇒ {
    t match {
      case a:Action if actionsEnabled ⇒ Full(JsonResponse(Json.serialise(Runner.run(a, req))))
      case c:Check if checksEnabled ⇒ Full(JsonResponse(Json.serialise(Runner.run(c))))
      case x => throw new RuntimeException("I don't know how to run a: " + x)
    }
  }
}


//E.G.
//import ServiceHelper._
//
////http://localhost:4253/booking/metadata
//object Booking extends ServiceHelper(
//  base = "booking",
//  version = "10001",
//  checksEnabled = true,
//  actionsEnabled = true
//)(
//    "make/payment/" action MakePayment
//  )
//
//case object MakePayment extends Action {
//  override def run(in: List[In]) = success(None)
//}