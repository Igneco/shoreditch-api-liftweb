package im.mange.shoreditch.api.liftweb

import im.mange.shoreditch.Shoreditch
import im.mange.shoreditch.api._
import im.mange.shoreditch.api.liftweb.EnhancedRestHelper._
//import net.liftweb.http.{JsonResponse, LiftResponse, Req}
//import net.liftweb.common.{Box, Full}
//import net.liftweb.json.JValue

//TODO: ultimate rename Shoreditch and have one import
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

//TODO: ultimately rename me ...
abstract class ServiceHelper(longName: String, alias: String, base: String, version: String, checksEnabled: Boolean, actionsEnabled: Boolean)(routes: Route[Service]*)
  extends EnhancedRestHelper[Service](longName, alias, base, "metadata", version)(routes: _*) {

  val shoreditch = Shoreditch(base, version, longName, alias, "metadata", routes)

  type ShoreditchResponse = () ⇒ String

  private val basePathParts = splitPath(base)

//  def xform(req: Request): Service ⇒ ShoreditchResponse

  private val rebasedRoutes: Seq[Route[Service]] = routes.map { _ withBase basePathParts }

  //TODO: two things in here might explain the bogus GET listings we get ...
  private def summaryHandler(req: Request): Option[ShoreditchResponse] = {
    //TODO: not sure about this check actually ... it's stupid, remove it ...
    if(shoreditch.summary.isEmpty) None
    else {
      val summaryResponse: ShoreditchResponse = () => {
        val theActions = shoreditch.actions.map(a => ActionMetaData(a._1, a._2.parameters.in, a._2.parameters.out)).toList
        val theChecks = shoreditch.checks.map(c => CheckMetaData(c._1)).toList

        val metaData = MetaDataResponse(longName, alias, version, theChecks, theActions)
        Json.serialise(metaData)
      }
      val summaryRoute: Route[ShoreditchResponse] = GET0(shoreditch.summary) {
        summaryResponse
      } withBase basePathParts
      summaryRoute.attemptMatch(req)
    }
  }

  private val matchers: Seq[(Request) => Option[Service]] = rebasedRoutes map { r ⇒ r.attemptMatch _ }

  private def lazyAppliedMatches(req: Request) = matchers.iterator map { _(req) }
  private def firstMatchingRoute(req: Request) = lazyAppliedMatches(req).find(_.isDefined).flatten

  def handler(req: Request) : Option[ShoreditchResponse] =
    firstMatchingRoute(req).map(xform(req)) orElse summaryHandler(req)

  def xform(req: Request): (Service) => () => String = mkRunFunc(_, req)

  private def mkRunFunc(t: Service, req: Request): () ⇒ String = () ⇒ {
    t match {
      case a:Action if actionsEnabled ⇒ Json.serialise(Runner.run(a, req))
      case c:Check if checksEnabled ⇒ Json.serialise(Runner.run(c))
      case x => throw new RuntimeException("I don't know how to run a: " + x)
    }
  }
}