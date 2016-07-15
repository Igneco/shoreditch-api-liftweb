package im.mange.shoreditch.api.liftweb

import net.liftweb.http._

object EnhancedRestHelper {
  sealed trait PathPart { def simpleString: String }
  case class StaticPathPart(str: String) extends PathPart { def simpleString = str }
  case class DynPathPart(name: String) extends PathPart { def simpleString = "@" + name }

  def splitPath(str: String): List[PathPart] = {
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

    //TODO: this is nasty - this should have an escape after too many attempts...
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

  def POST[Service](pathstr: String)(fn: PartialFunction[List[String],Service]): Route[Service] =
    Route(PostRequest, pathstr, fn)

  def POST0[Service](pathstr: String)(fn: ⇒ Service): Route[Service] = POST(pathstr){ case Nil ⇒ fn }

  def OPTIONS[Service](pathstr: String)(fn: PartialFunction[List[String],Service]): Route[Service] =
    Route(OptionsRequest, pathstr, fn)

  def OPTIONS0[Service](pathstr: String)(fn: ⇒ Service): Route[Service] = OPTIONS(pathstr){case Nil ⇒ fn}

  def GET[Service](pathstr: String)(fn: PartialFunction[List[String],Service]): Route[Service] = Route(GetRequest, pathstr, fn)
  def GET0[Service](pathstr: String)(fn: ⇒ Service): Route[Service] = GET(pathstr){ case Nil ⇒ fn }
  def GET1[Service](pathstr: String)(fn: String ⇒ Service): Route[Service] = GET(pathstr){ case List(x) ⇒ fn(x) }
  def GET2[Service](pathstr: String)(fn: (String,String) ⇒ Service): Route[Service] = GET(pathstr){ case List(x1,x2) ⇒ fn(x1,x2) }
}