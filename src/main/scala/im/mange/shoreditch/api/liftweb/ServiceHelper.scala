package im.mange.shoreditch.api.liftweb

import im.mange.shoreditch.Shoreditch
import im.mange.shoreditch.api._
import im.mange.shoreditch.api.liftweb.EnhancedRestHelper._
import scala.collection.concurrent

//TODO: ultimate rename Shoreditch and have one import
object ServiceHelper {
  implicit class CheckRouteBuildingString(val path: String) extends AnyVal {
    def action(probeFn:               ⇒ Action): Route[Service] = POST0("action/" + path)(probeFn)//,
    def check(probeFn:                 ⇒ Check): Route[Service] = GET0("check/" + path)(probeFn)
    def check(probeFn: (String)        ⇒ Check): Route[Service] = GET1("check/" + path)(probeFn)
    def check(probeFn: (String,String) ⇒ Check): Route[Service] = GET2("check/" + path)(probeFn)
  }
}

//TODO: ultimately rename me ...
//TODO: move this stuff into a ShoreditchHandler() and have minimal stuff in Shoreditch() itself
//TODO: and pass in a Shoreditch ..
abstract class ServiceHelper(longName: String, alias: String, base: String, version: String, checksEnabled: Boolean, actionsEnabled: Boolean)(routes: Route[Service]*) {
  val shoreditch = Shoreditch(base, version, longName, alias, "metadata", routes)

  var actions = concurrent.TrieMap[String, Action]()
  var checks = concurrent.TrieMap[String, Check]()

  //TODO: should be foreach
  routes.map(r =>
    r.service match {
      case a:Action => actions.update(base + "/" + r.pathStr, a)
      case c:Check => checks.update(base + "/" + r.pathStr, c)
      case x => //???
    })


  def handler(req: Request) : Option[ShoreditchResponse] =
    firstMatchingRoute(req).map(xform(req)) orElse summaryHandler(req)

  type ShoreditchResponse = () ⇒ String

  private val basePathParts = splitPath(base)

  private val rebasedRoutes: Seq[Route[Service]] = routes.map { _ withBase basePathParts }

  //TODO: two things in here might explain the bogus GET listings we get ...
  private def summaryHandler(req: Request): Option[ShoreditchResponse] = {
    //TODO: not sure about this check actually ... it's stupid, remove it ...
    if(shoreditch.summary.isEmpty) None
    else {
      val summaryResponse: ShoreditchResponse = () => {
        val theActions = actions.map(a => ActionMetaData(a._1, a._2.parameters.in, a._2.parameters.out)).toList
        val theChecks = checks.map(c => CheckMetaData(c._1)).toList

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

  private def xform(req: Request): (Service) => () => String = mkRunFunc(_, req)

  private def mkRunFunc(t: Service, req: Request): () ⇒ String = () ⇒ {
    t match {
      case a:Action if actionsEnabled ⇒ Json.serialise(Runner.run(a, req))
      case c:Check if checksEnabled ⇒ Json.serialise(Runner.run(c))
      case x => throw new RuntimeException("I don't know how to run a: " + x)
    }
  }
}