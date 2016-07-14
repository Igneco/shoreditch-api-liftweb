package im.mange.shoreditch.api.liftweb

import im.mange.shoreditch.api._
import net.liftweb.common.{Box, Full}
import net.liftweb.http._
import scala.collection.concurrent

//TODO: rename to AutoIndexRestHelper or something
object EnhancedRestHelper {
  sealed trait PathPart { def simpleString: String }
  case class StaticPathPart(str: String) extends PathPart { def simpleString = str }
  case class DynPathPart(name: String) extends PathPart { def simpleString = "@" + name }

  private def splitPath(str: String): List[PathPart] = {
    val pathParts: Array[PathPart] = str split '/' map {
      case x if x startsWith "@" ⇒ DynPathPart(x.tail)
      case x ⇒ StaticPathPart(x)
    }
    pathParts.toList
  }

  object Route {
    def apply[Service](rt: RequestType, path: String, fn: PartialFunction[List[String], Service]): Route[Service] = {
      new Route[Service](rt, splitPath(path), fn)
    }
  }

  class Route[Service] private (rt: RequestType, pathParts: List[PathPart], fn: PartialFunction[List[String], Service]) {
    lazy val pathStr = pathParts.map(_.simpleString).mkString("/")

    //TODO: this is nasty
    //TODO: this should have an escape after too many attempts...
    val service = {
      var attempt: List[String] = Nil
      while (!fn.isDefinedAt(attempt)) {
        attempt = "?" :: attempt
      }
      fn.apply(attempt)
    }

    @annotation.tailrec
    private def recMatch(pairs: List[(PathPart,String)], acc: List[String] = Nil): Option[List[String]] =
      pairs match {
        case (StaticPathPart(exp), act) :: tail if exp == act ⇒ recMatch(tail, acc)
        case (StaticPathPart(exp), act) :: tail ⇒ None
        case (DynPathPart(_), act) :: tail ⇒ recMatch(tail, act :: acc)
        case Nil ⇒ Some(acc.reverse)
        case _ ⇒ ???
      }

    def attemptMatch(req: Request) : Option[Service] = {
      req match {
        case Req(inboundPathParts, _, `rt`) ⇒
        //        val pairs = pathParts zip inboundPathParts
        //        val theMatch = recMatch(pairs)
        //        theMatch map attemptFn
        case _ ⇒ None
      }
        val pairs = pathParts zip req.inboundPathParts
        val theMatch = recMatch(pairs)
        theMatch map attemptFn
    }

    private def attemptFn(xs: List[String]): Service =
      if (fn.isDefinedAt(xs)) { fn(xs) }
      else {
        throw new RuntimeException(s"The backing function for path $pathStr takes the wrong number of elements, but have: " + xs)
      }

    def withBase(base: List[PathPart]): Route[Service] = new Route(rt, base ::: pathParts, fn)
  }

  def POST[Service](pathstr: String)(fn: PartialFunction[List[String],Service]): Route[Service] = {
    Route(PostRequest, pathstr, fn)
  }

  def POST0[Service](pathstr: String)(fn: ⇒ Service): Route[Service] = POST(pathstr){ case Nil ⇒ fn }

  def OPTIONS[Service](pathstr: String)(fn: PartialFunction[List[String],Service]): Route[Service] = {
    Route(OptionsRequest, pathstr, fn)
  }

  def OPTIONS0[Service](pathstr: String)(fn: ⇒ Service): Route[Service] = OPTIONS(pathstr){case Nil ⇒ fn}

  def GET[Service](pathstr: String)(fn: PartialFunction[List[String],Service]): Route[Service] = Route(GetRequest, pathstr, fn)
  def GET0[Service](pathstr: String)(fn: ⇒ Service): Route[Service] = GET(pathstr){ case Nil ⇒ fn }
  def GET1[Service](pathstr: String)(fn: String ⇒ Service): Route[Service] = GET(pathstr){ case List(x) ⇒ fn(x) }
  def GET2[Service](pathstr: String)(fn: (String,String) ⇒ Service): Route[Service] = GET(pathstr){ case List(x1,x2) ⇒ fn(x1,x2) }
}

import im.mange.shoreditch.api.liftweb.EnhancedRestHelper._

abstract class EnhancedRestHelper[Service](longName: String = "", alias: String = "", base: String = "", summary: String = "", version: String)(routes: Route[Service]*) extends RestHelper {
  var actions = concurrent.TrieMap[String, Action]()
  var checks = concurrent.TrieMap[String, Check]()

  routes.map(r =>
    r.service match {
    case a:Action => actions.update(base + "/" + r.pathStr, a)
    case c:Check => checks.update(base + "/" + r.pathStr, c)
    case x => //???
  })

  type BoxedLiftResponse = () ⇒ LiftResponse

  private val basePathParts = splitPath(base)

  def xform(req: Request): Service ⇒ BoxedLiftResponse

  private val rebasedRoutes: Seq[Route[Service]] = routes.map { _ withBase basePathParts }

  //TODO: two things in here might explain the bogus GET listings we get ...
  private def summaryHandler(req: Request): Option[BoxedLiftResponse] = {
    //TODO: not sure about this check actually ...
    if(summary.isEmpty) None
    else {
      val summaryResponse: BoxedLiftResponse = () => {
        val theActions = actions.map(a => ActionMetaData(a._1, a._2.parameters.in, a._2.parameters.out)).toList
        val theChecks = checks.map(c => CheckMetaData(c._1)).toList

        val metaData = MetaDataResponse(longName, alias, version, theChecks, theActions)
        JsonResponse(Json.serialise(metaData))
      }
      val summaryRoute: Route[BoxedLiftResponse] = GET0(summary) {
        summaryResponse
      } withBase basePathParts
      summaryRoute.attemptMatch(req)
    }
  }

  private val matchers: Seq[(Request) => Option[Service]] = rebasedRoutes map { r ⇒ r.attemptMatch _ }

  private def lazyAppliedMatches(req: Request) = matchers.iterator map { _(req) }
  private def firstMatchingRoute(req: Request) = lazyAppliedMatches(req).find(_.isDefined).flatten

  def handler(req: Request) : Option[BoxedLiftResponse] =
    firstMatchingRoute(req).map(xform(req)) orElse summaryHandler(req)

  //TODO: ultimately this must die ... but it is kind of important!
//  serve {
//    Function unlift handler
//  }
}