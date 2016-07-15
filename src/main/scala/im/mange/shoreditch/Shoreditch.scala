package im.mange.shoreditch

import scala.collection.concurrent
import im.mange.shoreditch.api.{Action, Check}
import im.mange.shoreditch.api.liftweb.EnhancedRestHelper._

//TODO: should be no api.liftwen deps in here
//TODO: see if we can whack summary
//TODO: decide what is mandatory and use Options
case class Shoreditch[Service](base: String, version: String, longName: String = "", alias: String = "", summary: String = "", routes: Seq[Route[Service]]) {
  var actions = concurrent.TrieMap[String, Action]()
  var checks = concurrent.TrieMap[String, Check]()

  //TODO: should be foreach
  routes.map(r =>
    r.service match {
      case a:Action => actions.update(base + "/" + r.pathStr, a)
      case c:Check => checks.update(base + "/" + r.pathStr, c)
      case x => //???
    })
}
